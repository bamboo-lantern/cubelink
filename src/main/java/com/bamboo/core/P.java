package com.bamboo.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 模板类
 */

public class P {

    // html标签名称
    protected String element;
    // 标签的属性 <- 懒得再建一个A对象用来存放属性，先用字符串操作吧。。。
    protected final Map<String, String> attributes = new HashMap<>();
    // 标签的内容
    protected final List<Object> pList = new ArrayList<>();
    // 父标签
    protected P parent = null;

    public P() {
        this.element = "div";
    }

    /**
     * e -> element
     */
    public P e(String element) {
        this.element = element;
        return this;
    }

    /**
     * t -> template
     */
    public P t(Object o) {
        pList.add(o);
        if (o instanceof P) {
            ((P) o).setParent(this);
        }
        return this;
    }

    /**
     * 字符串模板的占位符写法，占位符为"{}"
     */
    public P t(String s, String...ss) {
        int i = 0;
        while(i < ss.length) {
            s = s.replaceFirst("\\{\\}", ss[i++]);
        }
        pList.add(s);
        return this;
    }

    public P t(AnInterfaceInMethodT iT) {
        Object o = iT.t(this);
        if (o instanceof P) {
            ((P) o).setParent(this);
        }
        pList.add(o);
        return this;
    }

    /**
     * 使template可以嵌入逻辑语句，并用lambda表达式简化语法
     */
    public interface AnInterfaceInMethodT {
        Object t(P p);
    }

    /**
     * a -> attribute
     */
    public P a(String name, String... value) {
        StringBuilder attrValue = new StringBuilder();
        for (String attrVal : value) {
            attrValue.append(attrVal).append(" ");
        }
//        attributes.put(name, attributes.get(name) != null ? attributes.get(name) + attrValue.toString() : attrValue.toString());
        attributes.merge(name, attrValue.toString(), (a, b) -> a + b);
        return this;
    }
    
    public P style(String name, String value) {
        attributes.merge("style", name + ": " + value + ";", (a, b) -> a + b);
        return this;
    }
    
    

    /**
     * 渲染为html字符串
     */
    public String toString() {
        // 先处理属性
        StringBuffer attr = new StringBuffer();
        attributes.forEach((key, value) -> attr.append(" ").append(key).append("=\"").append(value.trim()).append("\""));

        StringBuffer htmlStr = new StringBuffer();
        htmlStr.append("<").append(element).append(attr).append(">");
        pList.forEach(p -> htmlStr.append(p.toString()));
        htmlStr.append("</").append(element).append(">");
        return htmlStr.toString();
    }


    protected String getElement() {
        return element;
    }

    protected List<Object> getPList() {
        return pList;
    }

    protected Map<String, String> getAttributes() {
        return attributes;
    }

    protected P getParent() {
        return parent;
    }

    protected void setParent(P parent) {
        this.parent = parent;
    }

    protected Object getLastChild() {
        return pList.get(pList.size()-1);
    }


    /**
     * ************************  下面都是语法糖，简化写法  ************************
     */

    public P id(String s) {
        return a("id", s);
    }

    public P clazz(String s) {
        return a("class", s);
    }

    public P name(String s) {
        return a("name", s);
    }

    public P width(String s) {
        return style("width", s);
    }

    public P height(String s) {
        return style("height", s);
    }
}
