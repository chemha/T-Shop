package org.tshop.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.tshop.services.ProductService;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/")
public class HomeController {
    @Autowired
    ProductService productService;
    @GetMapping
    public String home(Model model, HttpSession session) {
        model.addAttribute("products", productService.findAll());

        return "home";
    }
}
