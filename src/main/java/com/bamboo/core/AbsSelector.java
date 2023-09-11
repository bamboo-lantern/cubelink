package com.bamboo.core;

/**
 * 选择器，目标是可以像JQuery的选择器一样简单的获取dom节点并操作
 */
public abstract class AbsSelector extends P implements ISelector{

    @Override
    public void render(String express, Object o) {
        $(express).render(o);
    }
}
