package com.bamboo.core;

import java.io.IOException;

/**
 * 选择器接口
 */
public interface ISelector {

    // 从文件或字符串读取dom
    void setDom(String pathORhtmlStr) throws IOException;

    // 从P模板直接获取dom
    void setDom(P templateP);

    // 类似JQuery的选择器，用于获取指定的P模板对象
    ISelector $(String expresses);

    // 类似JQuery的写法，ISelector.$("表达式").render(p)
    void render(Object o);

    // 将P模板渲染到指定标签下，express为筛选表达式
    void render(String express, Object o);
}
