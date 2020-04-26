package com.custom.arouter_compiler;

import com.custom.arouter_annotation.ARouter;
import com.custom.arouter_annotation.bean.RouterBean;
import com.custom.arouter_compiler.utils.ProcessorConfig;
import com.custom.arouter_compiler.utils.ProcessorUtils;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

import org.checkerframework.checker.units.qual.Area;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
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
@SupportedAnnotationTypes({ProcessorConfig.AROUTER_PACKAGE})

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
public class ARouterProcessor extends AbstractProcessor {


    /**
     * 操作Element的工具类（类，函数，属性，其实都是Element）
     */
    private Elements elementTool;

    /**
     * type(类信息)的工具类，包含用于操作TypeMirror的工具方法
     */
    private Types typeTool;

    /**
     * Message用来打印 日志相关信息
     */
    private Messager messager;

    /**
     * 文件生成器， 类 资源 等，就是最终要生成的文件 是需要Filer来完成的
     */
    private Filer filer;

    /**
     * （模块传递过来的）模块名  app，personal
     */
    private String options;

    /**
     * （模块传递过来的） 包名
     */
    private String aptPackage;

    /**
     * 仓库一  PATH
     * key : group分组
     * value：需要添加到对应分组的注册的类信息
     */
    private Map<String, List<RouterBean>> allPathMap = new HashMap<>();

    /**
     * 仓库二 GROUP
     * key : group分组
     * value: apt生成的类的全类名，如：com.custom.ARouter$$Path$$User
     */
    private Map<String, String> allGroupMap = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        elementTool = processingEnv.getElementUtils();
        messager = processingEnv.getMessager();
        filer = processingEnv.getFiler();
        typeTool = processingEnv.getTypeUtils();

        options = processingEnv.getOptions().get(ProcessorConfig.OPTIONS);
        aptPackage = processingEnv.getOptions().get(ProcessorConfig.APT_PACKAGE);

        messager.printMessage(Diagnostic.Kind.NOTE, ">>>>>>>>>>>>>>>>>>>>>> options:" + options);
        messager.printMessage(Diagnostic.Kind.NOTE, ">>>>>>>>>>>>>>>>>>>>>> aptPackage:" + aptPackage);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty()) {
            messager.printMessage(Diagnostic.Kind.NOTE, "没有发现@ARouter注解");
            return false;
        }

        // Activity type
        TypeElement activityType = elementTool.getTypeElement(ProcessorConfig.ACTIVITY_PACKAGE);
        TypeMirror activityMirror = activityType.asType();

        // 获取所有被 @ARouter 注解的 元素集合
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(ARouter.class);

        //todo 1.收集需要处理的注解相关的详细
        for (Element element : elements) {
            String className = element.getSimpleName().toString();
            messager.printMessage(Diagnostic.Kind.NOTE, "ARetuer注解的类：" + className);

            ARouter aRouter = element.getAnnotation(ARouter.class);

            RouterBean routerBean = new RouterBean.Builder()
                    .addGroup(aRouter.group())
                    .addPath(aRouter.path())
                    .addElement(element)
                    .build();

            TypeMirror elementMirror = element.asType();

            //判断是否是Activity类型
            if (typeTool.isAssignable(elementMirror, activityMirror)) {
                routerBean.setTypeEnum(RouterBean.TypeEnum.ACTIVITY);
            } else {
                throw new RuntimeException("@ARouter注解目前仅限用于Activity类之上");
            }

            if (checkRouterPath(routerBean)) {
                messager.printMessage(Diagnostic.Kind.NOTE, "RouterBean :" + routerBean.toString());

                String group = routerBean.getGroup();
                List<RouterBean> routerBeans = allPathMap.get(group);
                if (routerBeans == null) {
                    routerBeans = new ArrayList<>();
                    routerBeans.add(routerBean);
                    allPathMap.put(group, routerBeans);
                } else {
                    routerBeans.add(routerBean);
                }

            } else {
                messager.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解未按规范配置，如：/app/MainActivity");
            }
        }

        //todo 2.生成对应的文件
        try {
            // 定义（拿到标准 TYPE） PATH
            TypeElement pathType = this.elementTool.getTypeElement(ProcessorConfig.AROUTER_API_PATH);
            //  第一步：Path对应的类
            createPathFile(pathType);

            //  第二步：Group对应的类
            // 定义（拿到标准 TYPE） GROUP
            TypeElement groupType = this.elementTool.getTypeElement(ProcessorConfig.AROUTER_API_GROUP);
            createGroupFile(groupType, pathType);
        } catch (IOException e) {
            e.printStackTrace();
            messager.printMessage(Diagnostic.Kind.NOTE, "在生成PATH模板时，异常了 e:" + e.getMessage());
        }

        return true;
    }


    private boolean checkRouterPath(RouterBean routerBean) {
        String group = routerBean.getGroup();
        String path = routerBean.getPath();

        // @ARouter注解中的path值，必须要以 / 开头
        if (ProcessorUtils.isEmpty(path) || !path.startsWith("/")) {
            // ERROR 故意去奔溃的
            messager.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解中的path值，必须要以 / 开头");
            return false;
        }

        if (path.lastIndexOf("/") == 0) {
            // 架构师定义规范，让开发者遵循
            messager.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解未按规范配置，如：/app/MainActivity");
            return false;
        }

        String finalGroup = path.substring(1, path.indexOf("/", 1));

        if (!ProcessorUtils.isEmpty(group) && !group.equals(options)) {
            // 架构师定义规范，让开发者遵循
            messager.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解中的group值必须和子模块名一致！");
            return false;
        } else {
            // 设置group
            routerBean.setGroup(finalGroup);
        }

        return true;
    }


    private void createPathFile(TypeElement pathType) throws IOException {
        // 判断 map仓库中，是否有需要生成的文件
        if (ProcessorUtils.isEmpty(allPathMap)) {
            return;
        }

        // Map<String, RouterBean>  返回值
        TypeName methodReturn = ParameterizedTypeName.get(ClassName.get(Map.class)
                , ClassName.get(String.class)
                , ClassName.get(RouterBean.class));

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(ProcessorConfig.PATH_METHOD_NAME)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(methodReturn);


        // Map<String, RouterBean> pathMap = new HashMap<>();
        methodBuilder.addStatement("$T<$T, $T> $N = new $T<>()"
                , ClassName.get(Map.class)
                , ClassName.get(String.class)
                , ClassName.get(RouterBean.class)
                , ProcessorConfig.PATH_MAP
                , ClassName.get(HashMap.class));


        for (Map.Entry<String, List<RouterBean>> entry : allPathMap.entrySet()) {

            List<RouterBean> pathList = entry.getValue();
            for (RouterBean routerBean : pathList) {
                /**
                 *  pathMap.put("/order/Order_MainActivity",
                 *   RouterBean.create(RouterBean.TypeEnum.ACTIVITY, Order_MainActivity.class, "/order/Order_MainActivity", "order"));
                 */
                methodBuilder.addStatement("$N.put($S, $T.create($T.$L, $T.class, $S, $S))"
                        , ProcessorConfig.PATH_MAP
                        , routerBean.getPath()
                        , ClassName.get(RouterBean.class)
                        , ClassName.get(RouterBean.TypeEnum.class)
                        , routerBean.getTypeEnum()
                        , ClassName.get((TypeElement) routerBean.getElement())
                        , routerBean.getPath()
                        , routerBean.getGroup()
                );

            }
        }

        methodBuilder.addStatement("return $N", ProcessorConfig.PATH_MAP);

        // 最终生成的类文件名  ARouter$$Path$$  + Personal
        String finalClassName = ProcessorConfig.PATH_FILE_NAME + ProcessorUtils.captureName(options);

        messager.printMessage(Diagnostic.Kind.NOTE, ">>>>>>>>>>>>>>>>>>>>>> group :" + options);
        messager.printMessage(Diagnostic.Kind.NOTE, ">>>>>>>>>>>>>>>>>>>>>> finalClassName :" + finalClassName);

        // 生成 和 类 等等，结合一体
        TypeSpec classTypeSpec = TypeSpec.classBuilder(finalClassName)
                // 实现ARouterLoadPath接口
                .addSuperinterface(ClassName.get(pathType))
                // public修饰符
                .addModifiers(Modifier.PUBLIC)
                // 方法的构建（方法参数 + 方法体）
                .addMethod(methodBuilder.build())
                // 类构建完成
                .build();

        // 包名
        JavaFile.builder(aptPackage,
                // 类名
                classTypeSpec)
                // JavaFile构建完成
                .build()
                // 文件生成器开始生成类文件
                .writeTo(filer);

        allGroupMap.put(options, finalClassName);
    }


    private void createGroupFile(TypeElement groupType, TypeElement pathType) throws IOException {
        // 判断 map仓库中，是否有需要生成的文件
        if (ProcessorUtils.isEmpty(allGroupMap) || ProcessorUtils.isEmpty(allPathMap)) {
            return;
        }

        // 第二个参数：Class<? extends ARouterPath>
        // 某某Class是否属于ARouterLoadPath接口的实现类
        TypeName routerPathType = ParameterizedTypeName.get(ClassName.get(Class.class),
                WildcardTypeName.subtypeOf(ClassName.get(pathType)));

        // 设置返回类型： Map<String, Class<? extends ARouterPath>>
        TypeName typeName = ParameterizedTypeName.get(ClassName.get(Map.class)
                , ClassName.get(String.class)
                , routerPathType);

        MethodSpec.Builder methodSpecBuilder = MethodSpec.methodBuilder("getGroupMap")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(typeName);

        //Map<String, Class<? extends ARouterPath>> groupMap = new HashMap<>()
        methodSpecBuilder.addStatement("$T<$T, $T> $N = new $T<>()"
                , ClassName.get(Map.class)
                , ClassName.get(String.class)
                , routerPathType
                , ProcessorConfig.GROUP_MAP
                , ClassName.get(HashMap.class)
        );

        for (Map.Entry<String, String> entry : allGroupMap.entrySet()) {
            //groupMap.put("order", ARouter$$Path$$order.class)
            methodSpecBuilder.addStatement("$N.put($S, $T.class)"
                    , ProcessorConfig.GROUP_MAP
                    , entry.getKey()
                    // 类文件在指定包名下
                    , ClassName.get(aptPackage, entry.getValue())
            );
        }

        //return groupMap;
        methodSpecBuilder.addStatement("return $N", ProcessorConfig.GROUP_MAP);

        String finalClassName = ProcessorConfig.GROUP_FILE_NAME + ProcessorUtils.captureName(options);
        TypeSpec classTypeSpec = TypeSpec.classBuilder(finalClassName)
                // 实现ARouterGroup接口
                .addSuperinterface(ClassName.get(groupType))
                // public修饰符
                .addModifiers(Modifier.PUBLIC)
                // 方法的构建（方法参数 + 方法体）
                .addMethod(methodSpecBuilder.build())
                // 类构建完成
                .build();

        // 包名
        JavaFile.builder(aptPackage,
                // 类名
                classTypeSpec)
                // JavaFile构建完成
                .build()
                // 文件生成器开始生成类文件
                .writeTo(filer);
    }
}
