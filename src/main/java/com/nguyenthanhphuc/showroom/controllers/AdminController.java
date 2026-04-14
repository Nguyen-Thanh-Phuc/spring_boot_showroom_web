package com.nguyenthanhphuc.showroom.controllers;

import com.nguyenthanhphuc.showroom.models.GeneralInfo;
import com.nguyenthanhphuc.showroom.services.GeneralInfoService;
import com.nguyenthanhphuc.showroom.services.SettingsService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;

@Controller
public class AdminController {
    private final Auth auth;
    private final GeneralInfoService generalInfoService;
    private final SettingsService settingsService;
    private final String uploadDir;

    public AdminController(Auth auth, GeneralInfoService generalInfoService, SettingsService settingsService, @Value("${upload.dir:uploads}") String uploadDir) {
        this.auth = auth;
        this.generalInfoService = generalInfoService;
        this.settingsService = settingsService;
        this.uploadDir = uploadDir;
    }

    @GetMapping("/admin/general-info")
    public String generalInfo(Model model, HttpServletRequest request) {
        if (!auth.isUserLoggedIn(request)) {
            return "redirect:/login";
        }

        model.addAttribute("isUserLoggedIn", true);
        model.addAttribute("generalInfo", generalInfoService.getGeneralInfo());
        model.addAttribute("companyName", settingsService.loadCompanyName());
        return "admin-general-info";
    }

    @PostMapping("/admin/general-info")
    public String updateGeneralInfo(
            @RequestParam String shortName,
            @RequestParam String longName,
            @RequestParam(required = false) MultipartFile featureImage,
            Model model,
            HttpServletRequest request
    ) {
        if (!auth.isUserLoggedIn(request)) {
            return "redirect:/login";
        }

        var trimmedShortName = shortName == null ? "" : shortName.trim();
        var trimmedLongName = longName == null ? "" : longName.trim();
        var currentInfo = generalInfoService.getGeneralInfo();

        if (trimmedShortName.isBlank() || trimmedLongName.isBlank()) {
            model.addAttribute("errorMsg", "Company names cannot be empty.");
            model.addAttribute("isUserLoggedIn", true);
            model.addAttribute("generalInfo", new GeneralInfo(
                    trimmedShortName.isBlank() ? currentInfo.shortName() : trimmedShortName,
                    trimmedLongName.isBlank() ? currentInfo.longName() : trimmedLongName,
                    currentInfo.featureImagePath()
            ));
            model.addAttribute("companyName", settingsService.loadCompanyName());
            return "admin-general-info";
        }

        var featureImagePath = currentInfo.featureImagePath();
        if (featureImage != null && !featureImage.isEmpty()) {
            try {
                featureImagePath = storeFeatureImage(featureImage);
            } catch (IOException e) {
                model.addAttribute("errorMsg", "Failed to upload feature image.");
                model.addAttribute("isUserLoggedIn", true);
                model.addAttribute("generalInfo", currentInfo);
                model.addAttribute("companyName", settingsService.loadCompanyName());
                return "admin-general-info";
            }
        }

        generalInfoService.updateGeneralInfo(trimmedShortName, trimmedLongName, featureImagePath);
        model.addAttribute("successMsg", "General info updated.");
        model.addAttribute("isUserLoggedIn", true);
        model.addAttribute("generalInfo", generalInfoService.getGeneralInfo());
        model.addAttribute("companyName", settingsService.loadCompanyName());
        return "admin-general-info";
    }

    @GetMapping("/admin/about")
    public String about(Model model, HttpServletRequest request) {
        if (!auth.isUserLoggedIn(request)) {
            return "redirect:/login";
        }

        model.addAttribute("isUserLoggedIn", true);
        model.addAttribute("companyName", settingsService.loadCompanyName());
        model.addAttribute("aboutContent", settingsService.getAboutContent());
        return "admin-about";
    }

    @PostMapping("/admin/about")
    public String updateAbout(
            @RequestParam String aboutContent,
            Model model,
            HttpServletRequest request
    ) {
        if (!auth.isUserLoggedIn(request)) {
            return "redirect:/login";
        }

        try {
            settingsService.updateAboutContent(aboutContent);
            model.addAttribute("successMsg", "About content updated.");
        } catch (Exception e) {
            model.addAttribute("errorMsg", e.getMessage());
        }

        model.addAttribute("isUserLoggedIn", true);
        model.addAttribute("companyName", settingsService.loadCompanyName());
        model.addAttribute("aboutContent", settingsService.getAboutContent());
        return "admin-about";
    }

    private String storeFeatureImage(MultipartFile featureImage) throws IOException {
        var uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);

        var originalFilename = featureImage.getOriginalFilename();
        var safeOriginal = originalFilename == null ? "" : Paths.get(originalFilename).getFileName().toString();
        safeOriginal = safeOriginal.replaceAll("[^A-Za-z0-9._-]", "_");
        if (safeOriginal.isBlank()) {
            safeOriginal = "image";
        }
        var filename = "feature-" + Instant.now().toEpochMilli() + "-" + safeOriginal;

        Path destination = uploadPath.resolve(filename);
        try (var inputStream = featureImage.getInputStream()) {
            Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
        }

        return "/uploads/" + filename;
    }
}
