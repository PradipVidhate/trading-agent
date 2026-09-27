package com.pradip.tradingbot.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping({"/", "/ui"})
    public String dashboard() {
        return "index";
    }
}
