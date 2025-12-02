package com.springboot.springboothousemarket.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "redirect:/Web/HouseMarket/login.html";
    }

    @GetMapping("/index")
    public String index() {
        return "redirect:/Web/HouseMarket/login.html";
    }
}