package com.bamboo.core;


import org.junit.jupiter.api.Test;

import java.io.FileReader;
import java.io.IOException;

public class PTest {

    @Test
    public void PTest() {

        P p = new P();
        p
            .e("span")
            .t("一串字符")
            .t(new P()
                .e("div")
                .t("另{}字符", "一串"))
            .t(new P()
                .e("h2")
                .t("第三串字符")
                .a("id", "3rdStr")
                .a("class", "Str", "3rd")
                .style("color", "blue")
                .style("width", "66px"))
            .t((tp) -> this.hashCode() % 10 == 1 ? "1" : "!")
            .t((tp) -> {
                switch ((int) (System.currentTimeMillis() % 2)) {
                    case 0:
                        return "当前时间毫秒数为偶数";
                    case 1:
                        return "当前时间毫秒数为奇数";
                    default:
                        return "当前时间毫秒数不为整数，出大问题！！！";
                }
            });

        System.out.println(p.toString());
    }

    @Test
    public void placeHolderTest() {
        String s = "123{}abc{}ee";
//        String s = "123321";
        String[] ss = {"aaa", "bbb"};
        int i = 0;
        while (i < ss.length) {
            s = s.replaceFirst("\\{\\}", ss[i++]);
        }
        System.out.println(s);
    }

    @Test
    public void streamTest() throws IOException {

        P p = new P()
                .e("span")
                .t("一串字符")
                .t(new P()
                        .e("div")
                        .t("<--123 -->")
                        .t("另{}字符", "一串"))
                .t(new P()
                        .e("h2")
                        .t("第三串字符")
                        .a("id", "3rdStr")
                        .a("class", "Str", "3rd")
                        .style("color", "blue")
                        .style("width", "66px"))
                .t((tp) -> this.hashCode() % 10 == 1 ? "1" : "!")
                .t((tp) -> {
                    switch ((int) (System.currentTimeMillis() % 2)) {
                        case 0:
                            return "当前时间毫秒数为偶数";
                        case 1:
                            return "当前时间毫秒数为奇数";
                        default:
                            return "当前时间毫秒数不为整数，出大问题！！！";
                    }
                });

        String htmlStr = "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>Test</title>\n" +
                "</head>\n" +
                "<body>\n" +
                p.toString() +
                "<div />\n" +
                "</body>\n" +
                "</html>";

        FileReader stringReader = new FileReader("src\\test\\resources\\static\\index.html");

//        StringReader stringReader = new StringReader(htmlStr);

//        StringReader stringReader = new StringReader("abc123");

        int c;

        int labelPos = 0;               // 在上标签内>0，在标签外=0，在下标签内<0。标签内第一个单词附近(标签名)=1。标签属性=2。标签属性值=3。
        boolean inComment = false;      // 当前字符是否在注释中
        boolean isHtmlStart = false;        // 当前字符是否在<!DOCTYPE html>中间
        boolean inAttrVal = false;      // 当前字符是否在标签属性值中
        StringBuffer str1 = new StringBuffer();        // 当前文本1
        StringBuffer str2 = new StringBuffer();        // 当前文本2
        final P dom = new P().e("metaHTML");
        P cP = dom;         // currentP当前所在P对象

        while ((c = stringReader.read()) != -1) {

            // 当前字符在注释中
            if (inComment) {
                str1.appendCodePoint(c);
                int length = str1.length();
                if (str1.charAt(length-3) == '-' && str1.charAt(length-2) == '-' && str1.charAt(length-1) == '>') {
                    inComment = false;
                    cP.t("<{}",str1.toString());
                    str1 = new StringBuffer();
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
                    str1 = new StringBuffer();
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

                str1 = new StringBuffer();
                continue;
            }

            // 当前字符为下尖括号
            if (c == 62/*">"*/) {
                if (labelPos == -1) {
                    cP = cP.getParent();
                }
                // 处理标签内没有空格的情况(因为下方标签内赋值是在当前字符为空格时进行的)
                if (labelPos == 1 && str1.length()>0) {
                    cP = (P) cP.t(new P().e(str1.toString())).getLastChild();
                    // 处理单个标签(无对应下标签)的情况
                    if (str1.charAt(str1.length()-1) == '/') {
                        cP = cP.getParent();
                    }
                }

                labelPos = 0;

                str1 = new StringBuffer();
                str2 = new StringBuffer();
                continue;
            }

            // 当前字符在上下尖括号中间
            if (labelPos >0) {
                // 判断文本头<!DOCTYPE html>
                if (c == 33/*"!"*/) {
                    isHtmlStart = true;
                }

                // 判断注释
                if (labelPos == 1 && str1.length()>1 && str1.charAt(0) == '-' && str1.charAt(1) == '-') {
                    inComment = true;
                    str1.appendCodePoint(c);
                    continue;
                }
                // 判断下标签
                if (labelPos == 1 && c == 47/*"/"*/) {
                    labelPos = -1;
                    continue;
                }

                // key="value" 中间的"="号
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
                if (labelPos == 1 && str1.length()>0) {
                    cP = (P) cP.t(new P().e(str1.toString())).getLastChild();
                    labelPos = 2;
                    str1 = new StringBuffer();
                    continue;
                }
            }

            // 当前字符在尖括号外
            if (labelPos == 0) {
                str1.appendCodePoint(c);
            }
        }

        System.out.println(dom.toString());
    }



/**              <: 60
                 >: 62
                 -: 45
                 !: 33
                 /: 47
                  : 32
                 =: 61
                 ": 34
                 :: 58
 */

    @Test
    public void soutChar2Int() {
        String s = "<>-!/ =\":";
        char[] chars = s.toCharArray();
        for (char c :
            chars) {
            System.out.println(c + ": " + (int) c);
        }
    }

    @Test
    public void SelectorTest() {
        DomSelector domSelector;
        try {
            domSelector = new DomSelector("src\\test\\resources\\static\\index.html");
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        domSelector.render("div,span,#3rdStr",
            new P()
                .e("bbb")
                .t("插入一条字符串。")
                .t(new P()
                    .a("id", "new1")
                    .e("cccc")
                    .t("插入另一条字符串。"))
        );

        domSelector.$("#new1").render("再次插入的字符串。");

        System.out.println(domSelector.toString());
    }
}
