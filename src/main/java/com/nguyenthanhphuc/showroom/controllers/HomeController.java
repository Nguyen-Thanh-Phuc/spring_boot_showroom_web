package com.nguyenthanhphuc.showroom.controllers;

import jakarta.servlet.http.HttpServletRequest;
import com.nguyenthanhphuc.showroom.services.GeneralInfoService;
import com.nguyenthanhphuc.showroom.services.ProductsService;
import com.nguyenthanhphuc.showroom.services.SettingsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {
    private final Auth auth;
    private final GeneralInfoService generalInfoService;
    private final SettingsService settingsService;
    private final ProductsService productsService;

    public HomeController(Auth auth, GeneralInfoService generalInfoService, SettingsService settingsService, ProductsService productsService) {
        this.auth = auth;
        this.generalInfoService = generalInfoService;
        this.settingsService = settingsService;
        this.productsService = productsService;
    }

    @GetMapping("/")
    public String home(Model model, HttpServletRequest request, @RequestParam(required = false) String q) {
        model.addAttribute("isUserLoggedIn", auth.isUserLoggedIn(request));
        model.addAttribute("generalInfo", generalInfoService.getGeneralInfo());
        model.addAttribute("companyName", settingsService.loadCompanyName());
        model.addAttribute("aboutContent", settingsService.getAboutContent());
        model.addAttribute("productQuery", q == null ? "" : q);
        model.addAttribute("products", productsService.searchProducts(q));
        return "home";
    }
}
