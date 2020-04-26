package com.custom.arouter_annotation.bean;

import javax.lang.model.element.Element;


/**
 * 路由路径Path的最终实体封装类
 *
 * @author Administrator
 */
public class RouterBean {

    public enum TypeEnum {
        ACTIVITY
    }

    private RouterBean(TypeEnum typeEnum, Class<?> targetClass, String path, String group) {
        this.typeEnum = typeEnum;
        this.targetClass = targetClass;
        this.path = path;
        this.group = group;
    }

    public static RouterBean create(TypeEnum type, Class<?> targetClass, String path, String group) {
        return new RouterBean(type, targetClass, path, group);
    }

    /**
     * 构建者模式
     */
    private RouterBean(Builder builder) {
        this.typeEnum = builder.type;
        this.element = builder.element;
        this.targetClass = builder.targetClass;
        this.path = builder.path;
        this.group = builder.group;
    }

    /**
     * 枚举类型：Activity
     */
    private TypeEnum typeEnum;
    /**
     * 类节点 JavaPoet，可以拿到很多的信息
     */
    private Element element;
    /**
     * 被注解的 Class对象 例如： MainActivity.class  Main2Activity.class
     */
    private Class<?> targetClass;
    /**
     * 路由地址  例如：/app/MainActivity
     */
    private String path;

    /**
     * 路由组  例如：app  order  personal
     */
    private String group;

    public TypeEnum getTypeEnum() {
        return typeEnum;
    }

    public void setTypeEnum(TypeEnum typeEnum) {
        this.typeEnum = typeEnum;
    }

    public Element getElement() {
        return element;
    }

    public void setElement(Element element) {
        this.element = element;
    }

    public Class<?> getTargetClass() {
        return targetClass;
    }

    public void setTargetClass(Class<?> targetClass) {
        this.targetClass = targetClass;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }


    public static class Builder {

        /**
         * 枚举类型：Activity
         */
        private TypeEnum type;
        /**
         * 类节点
         */
        private Element element;
        /**
         * 注解使用的类对象
         */
        private Class<?> targetClass;
        /**
         * 路由地址
         */
        private String path;
        /**
         * 路由组
         */
        private String group;

        public Builder addType(TypeEnum type) {
            this.type = type;
            return this;
        }

        public Builder addElement(Element element) {
            this.element = element;
            return this;
        }

        public Builder addClass(Class<?> targetClass) {
            this.targetClass = targetClass;
            return this;
        }

        public Builder addPath(String path) {
            this.path = path;
            return this;
        }

        public Builder addGroup(String group) {
            this.group = group;
            return this;
        }

        public RouterBean build() {
            return new RouterBean(this);
        }

        @Override
        public String toString() {
            return "RouterBean{" +
                    "path='" + path + '\'' +
                    ", group='" + group + '\'' +
                    '}';
        }

    }


    @Override
    public String toString() {
        return "RouterBean{" +
                "path='" + path + '\'' +
                ", group='" + group + '\'' +
                ", typeEnum='" + typeEnum + '\'' +
                ", element='" + element + '\'' +
                '}';
    }

}
