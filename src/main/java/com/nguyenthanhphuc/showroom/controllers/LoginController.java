package com.nguyenthanhphuc.showroom.controllers;

import com.nguyenthanhphuc.showroom.services.GeneralInfoService;
import com.nguyenthanhphuc.showroom.services.LoginService;
import com.nguyenthanhphuc.showroom.services.SettingsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {
    private final LoginService loginService;
    private final Auth auth;
    private final GeneralInfoService generalInfoService;
    private final SettingsService settingsService;

    public LoginController(LoginService loginService, Auth auth, GeneralInfoService generalInfoService, SettingsService settingsService) {
        this.loginService = loginService;
        this.auth = auth;
        this.generalInfoService = generalInfoService;
        this.settingsService = settingsService;
    }

    @GetMapping("/login")
    public String loginGet(HttpServletRequest request, Model model) {
        if (auth.isUserLoggedIn(request)) {
            return "redirect:/";
        }
        model.addAttribute("isUserLoggedIn", false);
        model.addAttribute("generalInfo", generalInfoService.getGeneralInfo());
        model.addAttribute("companyName", settingsService.loadCompanyName());
        return "login";
    }

    @PostMapping("/login")
    public String loginPost(
            @RequestParam String username,
            @RequestParam String password,
            HttpServletRequest request,
            HttpServletResponse response,
            Model model
    ) {
        if (auth.isUserLoggedIn(request)) {
            return "redirect:/";
        }

        if (loginService.checkLogin(username, password)) {
            auth.setUserLoggedIn(response, username);
            return "redirect:/";
        }

        model.addAttribute("errorMsg", "Wrong username or password");
        model.addAttribute("username", username);
        model.addAttribute("password", password);
        model.addAttribute("isUserLoggedIn", false);
        model.addAttribute("generalInfo", generalInfoService.getGeneralInfo());
        model.addAttribute("companyName", settingsService.loadCompanyName());

        return "login";
    }
}
