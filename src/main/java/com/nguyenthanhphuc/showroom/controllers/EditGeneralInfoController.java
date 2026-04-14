package com.nguyenthanhphuc.showroom.controllers;

import com.nguyenthanhphuc.showroom.services.CompanyName;
import com.nguyenthanhphuc.showroom.services.SettingsService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class EditGeneralInfoController {
    private final Auth auth;
    private final SettingsService settingsService;

    public EditGeneralInfoController(Auth auth, SettingsService settingsService) {
        this.auth = auth;
        this.settingsService = settingsService;
    }

    @GetMapping("/edit-general-info")
    public String get(HttpServletRequest request, Model model) {
        if (!auth.isUserLoggedIn(request)) {
            return "redirect:/login";
        }

        var companyName = settingsService.getCompanyName();
        model.addAttribute("isUserLoggedIn", true)
                .addAttribute("companyName", companyName);
        return "edit-general-info";
    }

    @PostMapping("/edit-general-info")
    public String post(
            @RequestParam String companyShortName,
            @RequestParam String companyLongName,
            @RequestParam MultipartFile featureImage,
            HttpServletRequest request,
            Model model
    ) {
        if (!auth.isUserLoggedIn(request)) {
            return "redirect:/login";
        }

        var companyName = new CompanyName(companyShortName, companyLongName);
        try {
            settingsService.updateGeneralInfo(companyName, featureImage);
            return "redirect:/";
        } catch (Exception e) {
            model.addAttribute("isUserLoggedIn", true)
                    .addAttribute("companyName", companyName)
                    .addAttribute("errorMsg", e.getMessage());
            return "edit-general-info";
        }
    }
}
