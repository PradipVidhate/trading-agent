package com.pradip.tradingbot.controller;

import java.io.IOException;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.pradip.tradingbot.dto.AccessTokenData;
import com.pradip.tradingbot.dto.LoginResult;
import com.pradip.tradingbot.service.AuthService;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public void login(HttpServletResponse response) throws IOException {
        response.sendRedirect(authService.getLoginUrl());
    }

    @GetMapping("/callback")
    public String callback(@RequestParam(value = "request_token", required = false) String requestToken,
                          @RequestParam(value = "status", required = false) String status,
                          @RequestParam(value = "action", required = false) String action,
                          @RequestParam(value = "type", required = false) String type) {

        if (requestToken == null || requestToken.isBlank()) {
            return "redirect:http://localhost:8081/ui";
        }

        LoginResult result = authService.login(requestToken);

        if (result == null || result.getSession() == null) {
            return "redirect:http://localhost:8081/ui?error=login";
        }

        return "redirect:http://localhost:8081/ui?login=success";
    }

    @GetMapping("/logout")
    @ResponseBody
    public String logout() {

        authService.logout();

        return "Logged Out Successfully";
    }

    @GetMapping("/session")
    @ResponseBody
    public Object session() {

        AccessTokenData data = authService.getCurrentSession();

        if (data == null) {
            return "No Active Session";
        }

        return data;
    }
}
