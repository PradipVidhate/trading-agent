package com.pradip.tradingbot.controller;

import java.io.IOException;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.pradip.tradingbot.dto.UserProfile;
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
    @ResponseBody
    public String callback(@RequestParam("request_token") String requestToken) {

        return authService.authenticate(requestToken);

    }
    
    @GetMapping("/profile")
    @ResponseBody
    public UserProfile profile() {

        return authService.getProfile();

    }
    
}