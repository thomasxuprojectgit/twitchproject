package com.twitchproject.jupiter.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Controller
public class LogoutController {

    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    public void logout(HttpServletRequest request, HttpServletResponse response
    ) {
        HttpSession session = request.getSession(false); // false is: if no session, return null(instead of creating new one)
        if (session != null) {
            session.invalidate(); // invalid session in server
        }

        // this cookie with null will override the cookie in user browser
        Cookie cookie = new Cookie("JSESSIONID", null); // cookie in browser will be deleted
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

    }
}

