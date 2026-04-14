package com.nguyenthanhphuc.showroom.controllers;

import com.nguyenthanhphuc.showroom.services.SettingsService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class EditAboutContentController {
    private final Auth auth;
    private final SettingsService settingsService;

    public EditAboutContentController(Auth auth, SettingsService settingsService) {
        this.auth = auth;
        this.settingsService = settingsService;
    }

    @GetMapping("/edit-about-content")
    public String get(HttpServletRequest request, Model model) {
        if (!auth.isUserLoggedIn(request)) {
            return "redirect:/login";
        }

        model.addAttribute("isUserLoggedIn", true)
                .addAttribute("companyName", settingsService.loadCompanyName())
                .addAttribute("aboutContent", settingsService.getAboutContent());
        return "edit-about-content";
    }

    @PostMapping("/edit-about-content")
    public String post(
            @RequestParam String aboutContent,
            HttpServletRequest request,
            Model model
    ) {
        if (!auth.isUserLoggedIn(request)) {
            return "redirect:/login";
        }

        try {
            settingsService.updateAboutContent(aboutContent);
            return "redirect:/";
        } catch (Exception e) {
            model.addAttribute("isUserLoggedIn", true)
                    .addAttribute("companyName", settingsService.loadCompanyName())
                    .addAttribute("aboutContent", aboutContent)
                    .addAttribute("errorMsg", e.getMessage());
            return "edit-about-content";
        }
    }
}
