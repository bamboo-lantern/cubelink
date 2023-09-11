package com.bamboo.core;

import java.io.*;
import java.util.*;

/**
 * 将html解析后装进此对象进行操作
 */
public class DomSelector extends AbsSelector {

    // 元标签，虽然作为子类可以直接访问，不过命个名可读性会好些吧。
    private final P dom = this.e("metaHTML");

    // 预备渲染的位置
    private final Set<P> preRenderTemplates = new HashSet<>();

    public DomSelector() {
    }

    public DomSelector(String pathORhtmlStr) throws IOException {
        setDom(pathORhtmlStr);
    }

    /**
     * 将html字符串或文件解析为P对象
     * ！需注意，解析器只有解析功能，并没有格式校验功能，请自行确认格式或者自行实现该类的父抽象类或接口
     */
    @Override
    public void setDom(String pathORhtmlStr) throws IOException {
        Reader reader;
        // 以左尖括号开头则默认传入的是html字符串，否则认为是html文件路径
        if (pathORhtmlStr.trim().startsWith("<")) {
            reader = new StringReader(pathORhtmlStr);
        } else {
            reader = new FileReader(pathORhtmlStr);
        }
        getTemplatePFromStream(reader);
    }

    /**
     * 从P对象读取dom
     */
    @Override
    public void setDom(P templateP) {
        dom.t(templateP);
    }

    @Override
    public void render(Object o) {
        preRenderTemplates.forEach(p ->p.t(o));
        preRenderTemplates.clear();
    }

    @Override
    public ISelector $(String expresses) {
        String[] split = expresses.split(",");

        List<String> idList = new ArrayList<>();
        List<String> classList = new ArrayList<>();
        List<String> elementList = new ArrayList<>();
        for (String express : split) {
            switch (express.substring(0, 1)) {
                case "#":
                    idList.add(express.substring(1));
                    break;
                case ".":
                    classList.add(express.substring(1));
                    break;
                case "*":
                    preRenderTemplates.addAll(selectAllElement());
                    return this;
                default:
                    elementList.add(express);
                    break;
            }
        }

        Set<P> temPSet;
        temPSet = selectAllElement();
        for (P p : temPSet) {
            Map<String, String> attrMap = p.getAttributes();
            // 换一种写法，也许效率会高一点？
            /*String[] ids = attrMap.get("id") == null ? new String[0] : attrMap.get("id").split(" ");
            for (String id : ids) {
                if (idList.contains(id)) {
                    preRenderTemplates.add(p);
                }
            }*/
            String ids = " " + attrMap.get("id") + " ";
            for (String id : idList) {
                if (ids.contains(" " + id + " ")) {
                    preRenderTemplates.add(p);
                }
            }


            /*String[] classes = attrMap.get("class") == null ? new String[0] : attrMap.get("class").split(" ");
            for (String clazz : classes) {
                if (classList.contains(clazz)) {
                    preRenderTemplates.add(p);
                }
            }*/
            String classes = " " + attrMap.get("class") + " ";
            for (String clazz : classList) {
                if (classes.contains(" " + clazz + " ")) {
                    preRenderTemplates.add(p);
                }
            }

            if (elementList.contains(p.getElement())) {
                preRenderTemplates.add(p);
            }
        }
        return this;
    }

    private Set<P> selectAllElement() {
        Set<P> tempPSet = select(dom);
        Set<P> tempNextPSet = new HashSet<>();
        Set<P> pSet = new HashSet<>(tempPSet);
        while (!tempPSet.isEmpty()) {
            for (P p : tempPSet) {
                Set<P> selectedPSet = select(p);
                pSet.addAll(selectedPSet);
                tempNextPSet.addAll(selectedPSet);
            }
            tempPSet = tempNextPSet;
            tempNextPSet = new HashSet<>();
        }
        return pSet;
    }

    /**
     * 选出该标签下的所有标签对象P
     */
    private Set<P> select(P p) {
        Set<P> pSet = new HashSet<>();
        for (Object o : p.getPList()) {
            if (o instanceof P) {
                pSet.add((P) o);
            }
        }
        return pSet;
    }

    /**
     * 解析html字符流
     */
    private void getTemplatePFromStream(Reader reader) throws IOException {
        int c;

        int labelPos = 0;                   // 在上标签内>0，在标签外=0，在下标签内<0。标签内第一个单词附近(标签名)=1。标签属性=2。标签属性值的双引号中=3。
        boolean inComment = false;          // 当前字符是否在注释中
        boolean isHtmlStart = false;        // 当前字符是否在<!DOCTYPE html>中间
        boolean inAttrVal = false;          // 当前字符是否在标签属性值中
        StringBuilder str1 = new StringBuilder();        // 缓存文本1
        StringBuilder str2 = new StringBuilder();        // 缓存文本2
        P cP = dom;                         // currentP当前所在标签(P模板对象)

        while ((c = reader.read()) != -1) {

            // 当前字符在注释中
            if (inComment) {
                str1.appendCodePoint(c);
                int length = str1.length();
                if (str1.charAt(length - 3) == '-' && str1.charAt(length - 2) == '-' && str1.charAt(length - 1) == '>') {
                    inComment = false;
                    cP.t("<{}", str1.toString());
                    str1 = new StringBuilder();
                }
                continue;
            }

            // 当前字符在标签属性值中
            if (inAttrVal) {
                if (labelPos != 3 && c == 34/*"*/) {
                    labelPos = 3;
                    continue;
                }
                if (c == 34/*"*/) {
                    cP.a(str1.toString(), str2.toString());
                    inAttrVal = false;
                    labelPos = 2;
                    str1 = new StringBuilder();
                    str2 = new StringBuilder();
                    continue;
                }
                str2.appendCodePoint(c);
                continue;
            }

            // 当前字符在<!DOCTYPE html>中
            if (isHtmlStart) {
                str1.appendCodePoint(c);
                if (c == 62/*">"*/) {
                    cP.t("<{}", str1.toString());
                    isHtmlStart = false;
                    labelPos = 0;
                    str1 = new StringBuilder();
                    continue;
                }
                continue;
            }

            // 当前字符为上尖括号
            if (c == 60/*"<"*/) {
                labelPos = 1;
                String s = str1.toString();
                if (!s.isEmpty()) {
                    // 先不管换行符
//                    cP.t(s.replace(System.lineSeparator(), ""));
                    cP.t(s);
                }

                str1 = new StringBuilder();
                continue;
            }

            // 当前字符为下尖括号
            if (c == 62/*">"*/) {
                if (labelPos == -1) {
                    cP = cP.getParent();
                }
                // 处理标签内没有空格的情况(因为下方标签内赋值是在当前字符为空格时进行的)
                if (labelPos == 1 && str1.length() > 0) {
                    cP = (P) cP.t(new P().e(str1.toString())).getLastChild();
                    // 处理单个标签(无对应下标签)的情况
                    if (str1.charAt(str1.length() - 1) == '/') {
                        cP = cP.getParent();
                    }
                }
                // 无下标签的特殊标签处理
                if (cP != null && "meta".equals(cP.getElement())) {
                    cP = cP.getParent();
                }

                labelPos = 0;

                str1 = new StringBuilder();
                str2 = new StringBuilder();
                continue;
            }

            // 当前字符在上下尖括号中间
            if (labelPos > 0) {

                // 判断文本头<!DOCTYPE html>
                if (c == 33/*"!"*/) {
                    isHtmlStart = true;
                }

                // 判断注释
                if (labelPos == 1 && str1.length() > 2 && str1.charAt(0) == '!' && str1.charAt(1) == '-' && str1.charAt(2) == '-') {
                    inComment = true;
                    labelPos = 0;
                    str1.appendCodePoint(c);
                    continue;
                }

                // 判断下标签
                if (labelPos == 1 && c == 47/*"/"*/) {
                    labelPos = -1;
                    continue;
                }

                // 属性 key="value" 中间的"="号
                if (c == 61/*"="*/) {
                    // 此时str1为key
                    inAttrVal = true;
                    continue;
                }

                // 取单词
                if (!Character.isWhitespace(c)) {
                    str1.appendCodePoint(c);
                    continue;
                }

                // 如果当前空字符，又取到了左尖括号后第一个单词，这个单词就是标签名。
                if (labelPos == 1 && str1.length() > 0) {
                    cP = (P) cP.t(new P().e(str1.toString())).getLastChild();
                    labelPos = 2;
                    str1 = new StringBuilder();
                    continue;
                }
            }

            // 当前字符在尖括号外
            if (labelPos == 0) {
                str1.appendCodePoint(c);
            }
        }
    }

    @Override
    public String toString() {
        StringBuffer htmlStr = new StringBuffer();
        pList.forEach(p -> htmlStr.append(p.toString()));
        return htmlStr.toString();
    }
}
