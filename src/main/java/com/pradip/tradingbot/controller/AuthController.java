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
    @ResponseBody
    public String callback(@RequestParam String request_token) {

        LoginResult result =
                authService.login(request_token);

        return """
                Login Successful

                User : %s

                Instruments Loaded : %d

                Automated setup is ready for NIFTY signal generation and Kite alert updates.
                """.formatted(
                result.getSession().getUserName(),
                result.getInstrumentCount());
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
