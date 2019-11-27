package com.kkaka.arouter_annotation.model;

import javax.lang.model.element.Element;

/**
 * @author Laizexin on 2019/11/22
 * @description 路由路径Path的最终实体封装类
 */
public class RouterBean {

    public enum Type{
        ACTIVITY
    }

    private Type type;
    //类节点
    private Element element;
    //类对象
    private Class<?> clazz;
    //路由地址
    private String path;
    //路由组
    private String group;

    private RouterBean() {
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setElement(Element element) {
        this.element = element;
    }

    public void setClazz(Class<?> clazz) {
        this.clazz = clazz;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public Type getType() {
        return type;
    }

    public Element getElement() {
        return element;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public String getPath() {
        return path;
    }

    public String getGroup() {
        return group;
    }

    public RouterBean(Builder builder) {
        this.type = builder.type;
        this.element = builder.element;
        this.clazz = builder.clazz;
        this.path = builder.path;
        this.group = builder.group;
    }

    private RouterBean(Type type, Class<?> clazz, String path, String group) {
        this.type = type;
        this.clazz = clazz;
        this.path = path;
        this.group = group;
    }

    public static RouterBean create(Type type, Class<?> clazz, String path, String group) {
        return new RouterBean(type, clazz, path, group);
    }

    public static final class Builder{
        private Type type;
        private Element element;
        private Class<?> clazz;
        private String path;
        private String group;

        public Builder setType(Type type) {
            this.type = type;
            return this;
        }

        public Builder setElement(Element element) {
            this.element = element;
            return this;
        }

        public Builder setClazz(Class<?> clazz) {
            this.clazz = clazz;
            return this;
        }

        public Builder setPath(String path) {
            this.path = path;
            return this;
        }

        public Builder setGroup(String group) {
            this.group = group;
            return this;
        }

        public RouterBean build(){
            if(path == null || path.length() == 0){
                throw new IllegalArgumentException("path is necessary");
            }
            return new RouterBean(this);
        }
    }

    @Override
    public String toString() {
        return "RouterBean{" +
                "path='" + path + '\'' +
                ", group='" + group + '\'' +
                '}';
    }
}
