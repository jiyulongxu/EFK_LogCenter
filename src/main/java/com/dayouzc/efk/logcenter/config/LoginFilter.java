package com.dayouzc.efk.logcenter.config;

import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * @author FanJiangFeng
 * @version 1.0.0
 * @ClassName LoginFilter.java
 * @Description TODO
 * @createTime 2021年06月15日 11:20:00
 */
@Component
@WebFilter(value = "/")
public class LoginFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request=(HttpServletRequest)servletRequest;
        HttpServletResponse response=(HttpServletResponse)servletResponse;
        HttpSession session = request.getSession();
        Object account = session.getAttribute("account");
        String url = request.getRequestURI();
        //放行静态资源
        if(url.endsWith(".docx")||url.endsWith(".css")||url.endsWith(".js")||url.endsWith(".jpg") ||url.endsWith(".gif")||url.endsWith(".png")||url.endsWith(".ico")){
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        if (url.equals("/page/login") || url.equals("/login/toLogin")) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;

        }
        if(account==null){
            //未登录，重定向到登录页面
        request.getRequestDispatcher("/page/login").forward(request,response);
        }else{
            //放行
            filterChain.doFilter(servletRequest,servletResponse);
        }
    }

    @Override
    public void destroy() {

    }
}
