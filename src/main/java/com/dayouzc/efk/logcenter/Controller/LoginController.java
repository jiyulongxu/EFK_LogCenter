package com.dayouzc.efk.logcenter.Controller;

import com.dayouzc.efk.logcenter.constant.APPEnums;
import com.dayouzc.efk.logcenter.constant.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

/**
 * @author FanJiangFeng
 * @version 1.0.0
 * @ClassName LoginController.java
 * @Description TODO
 * @createTime 2021年06月15日 10:59:00
 */
@RestController
@RequestMapping("/login")
public class LoginController {

    /**
     * todo 登录 只是模拟登录，后期完善登录逻辑
     */
    @PostMapping("/toLogin")
    public JsonObject toLogin(String username, String password, HttpSession session){
        if(StringUtils.equals(username,"admin") && StringUtils.equals(password,"admin")){
            session.setAttribute("account",username);
            return JsonObject.Success();
        }else{
            return new JsonObject(APPEnums.LOGIN_PASSWORD_ERROR);
        }
    }
}
