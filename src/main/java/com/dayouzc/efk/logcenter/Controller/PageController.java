package com.dayouzc.efk.logcenter.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author FanJiangFeng
 * @version 1.0.0
 * @ClassName QueryController.java
 * @Description TODO
 * @createTime 2021年06月08日 13:53:00
 */
@Controller
@RequestMapping("/page")
public class PageController {

    /**
     * 跳转首页
     * @return
     */
    @RequestMapping("/index")
    public String index(){
        return "index";
    }

    /**
     * 跳转日志高级检索页面
     */
    @RequestMapping("/searchPage")
    public String searchPage(){
        return "search_page";
    }
}
