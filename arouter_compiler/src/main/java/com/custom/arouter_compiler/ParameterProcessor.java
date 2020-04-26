package com.custom.arouter_compiler;


import com.custom.arouter_annotation.Parameter;
import com.custom.arouter_compiler.utils.ProcessorConfig;
import com.custom.arouter_compiler.utils.ProcessorUtils;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * AutoService则是固定的写法，加个注解即可
 * 通过auto-service中的@AutoService可以自动生成AutoService注解处理器，用来注册
 * 用来生成 META-INF/services/javax.annotation.processing.Processor 文件
 */
@AutoService(Processor.class)

/**
 * 添加需要处理的注解类对象
 */
@SupportedAnnotationTypes({ProcessorConfig.ANNOTATION_PARAMETER})

/**
 * 指定JDK编译版本
 */
@SupportedSourceVersion(SourceVersion.RELEASE_7)

/**
 * 注解处理器接收的参数
 */
@SupportedOptions({ProcessorConfig.OPTIONS, ProcessorConfig.APT_PACKAGE})

/**
 * @author Administrator
 */
public class ParameterProcessor extends AbstractProcessor {


    /**
     * 操作Element的工具类（类，函数，属性，其实都是Element）
     */
    private Elements elementUtils;

    /**
     * type(类信息)的工具类，包含用于操作TypeMirror的工具方法
     */
    private Types typeUtils;

    /**
     * Message用来打印 日志相关信息
     */
    private Messager messager;

    /**
     * 文件生成器， 类 资源 等，就是最终要生成的文件 是需要Filer来完成的
     */
    private Filer filer;

    /**
     * key:类节点
     * value:被@Parameter注解的属性集合
     */
    private Map<TypeElement, List<Element>> tempParameterMap = new HashMap<>();


    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        elementUtils = processingEnv.getElementUtils();
        messager = processingEnv.getMessager();
        filer = processingEnv.getFiler();
        typeUtils = processingEnv.getTypeUtils();

    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty()) {
            return false;
        }

        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Parameter.class);

        //todo 1.收集需要处理的注解相关的详细
        for (Element element : elements) {

            messager.printMessage(Diagnostic.Kind.NOTE, ">>>>>>>>>>>>>>>>>>>>>> ParameterName :" + element.getAnnotation(Parameter.class).name());

            // 注解在属性的上面，属性节点父节点 是 类节点
            TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

            if (tempParameterMap.containsKey(enclosingElement)) {
                tempParameterMap.get(enclosingElement).add(element);
            } else {
                List<Element> elementList = new ArrayList<>();
                elementList.add(element);
                tempParameterMap.put(enclosingElement, elementList);
            }
        }

        // 判断是否有需要生成的类文件
        if (ProcessorUtils.isEmpty(tempParameterMap)) {
            return true;
        }

        // 通过Element工具类，获取Parameter类型
        TypeElement activityType = elementUtils.getTypeElement(ProcessorConfig.ACTIVITY_PACKAGE);
        TypeElement parameterType = elementUtils.getTypeElement(ProcessorConfig.AROUTER_AIP_PARAMETER);

        //todo 2.生成对应的文件
        ParameterSpec parameterSpec = ParameterSpec.builder(TypeName.OBJECT
                , ProcessorConfig.PARAMETER_NAME)
                .build();

        MethodSpec.Builder methodSpecBuilder = MethodSpec.methodBuilder(ProcessorConfig.PARAMETER_METHOD_NAME)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(parameterSpec);


        for (Map.Entry<TypeElement, List<Element>> entry : tempParameterMap.entrySet()) {

            // Map集合中的key是类名，如：MainActivity
            TypeElement typeElement = entry.getKey();
            // 如果类名的类型和Activity类型不匹配
            if (!typeUtils.isSubtype(typeElement.asType(), activityType.asType())) {
                throw new RuntimeException("@Parameter注解目前仅限用于Activity类之上");
            }
            // 获取类名 == Order_MainActivity
            ClassName className = ClassName.get(typeElement);

            // Personal_MainActivity t = (Personal_MainActivity) targetParameter;
            methodSpecBuilder.addStatement("$T t = ($T) " + ProcessorConfig.PARAMETER_NAME
                    , className
                    , className
            );

            for (Element fieldElement : entry.getValue()) {

                // 遍历注解的属性节点 生成函数体
                TypeMirror typeMirror = fieldElement.asType();
                // 获取 TypeKind 枚举类型的序列号
                int type = typeMirror.getKind().ordinal();
                // 获取属性名
                String fieldName = fieldElement.getSimpleName().toString();
                // 获取注解的值
                String annotationValue = fieldElement.getAnnotation(Parameter.class).name();
                // 判断注解的值为空的情况下的处理（注解中有name值就用注解值）
                annotationValue = ProcessorUtils.isEmpty(annotationValue) ? fieldName : annotationValue;

                // t.name = t.getIntent().getStringExtra("name")
                if (type == TypeKind.INT.ordinal()) {
                    methodSpecBuilder.addStatement("t." + fieldName + " = t.getIntent().getIntExtra(\"" + annotationValue + "\",0)");
                } else if (type == TypeKind.BOOLEAN.ordinal()) {
                    methodSpecBuilder.addStatement("t." + fieldName + " = t.getIntent().getBooleanExtra(\"" + annotationValue + "\",false)");
                } else {
                    if (typeMirror.toString().equalsIgnoreCase(ProcessorConfig.STRING)) {
                        methodSpecBuilder.addStatement("t." + fieldName + " = t.getIntent().getStringExtra(\"" + annotationValue + "\")");
                    }
                }
            }

            String finalClassName = typeElement.getSimpleName() + ProcessorConfig.PARAMETER_FILE_NAME;
            TypeSpec classTypeSpec = TypeSpec.classBuilder(finalClassName)
                    // 实现ARouterGroup接口
                    .addSuperinterface(ClassName.get(parameterType))
                    // public修饰符
                    .addModifiers(Modifier.PUBLIC)
                    // 方法的构建（方法参数 + 方法体）
                    .addMethod(methodSpecBuilder.build())
                    // 类构建完成
                    .build();

            // 包名
            try {
                JavaFile.builder(className.packageName(),
                        // 类名
                        classTypeSpec)
                        // JavaFile构建完成
                        .build()
                        // 文件生成器开始生成类文件
                        .writeTo(filer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return true;
    }
}
