package com.custom.arouter_annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Administrator
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
public @interface Parameter {

    /**
     * name的注解值表示该属性名就是key，填写了就用注解值作为key
     * 从getIntent()方法中获取传递参数值
     *
     * @return
     */
    String name() default "";
}
