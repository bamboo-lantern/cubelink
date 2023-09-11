package com.bamboo.controller;

import com.bamboo.core.P;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class TestController {

    @ResponseBody
    @GetMapping("/test")
    public String test() {
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
            .t((t) -> this.hashCode() % 10 == 1 ? "1" : "!")
            .t((t) -> {
                switch ((int) (System.currentTimeMillis() % 2)) {
                    case 0:
                        return "当前时间毫秒数为偶数";
                    case 1:
                        return "当前时间毫秒数为奇数";
                    default:
                        return "当前时间毫秒数不为整数，出大问题！！！";
                }
            });
        return "<!DOCTYPE html>\n" +
            "<html lang=\"en\">\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <title>Test</title>\n" +
            "</head>\n" +
            p.toString() +
            "<body>\n" +
            "\n" +
            "</body>\n" +
            "</html>";
//        return p.toString();
    }
}
