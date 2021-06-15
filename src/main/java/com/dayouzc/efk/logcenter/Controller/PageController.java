package com.dayouzc.efk.logcenter.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;

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
    public String index(HttpSession session, Model model){
        Object account = session.getAttribute("account");
        if(account!=null){
            String username=account.toString();
            model.addAttribute("account",username);
        }
        return "index";
    }

    /**
     * 跳转日志高级检索页面
     */
    @RequestMapping("/searchPage")
    public String searchPage(HttpSession session, Model model){
        Object account = session.getAttribute("account");
        if(account!=null){
            String username=account.toString();
            model.addAttribute("account",username);
        }
        return "search_page";
    }

    /**
     * 跳转登录页面
     */
    @RequestMapping("/login")
    public String login(){
        return "login";
    }

    /**
     * 退出登录
     */
    @RequestMapping("/logout")
    public String logout(HttpSession session){
        session.removeAttribute("account");
        return "forward:/page/login";
    }
}
