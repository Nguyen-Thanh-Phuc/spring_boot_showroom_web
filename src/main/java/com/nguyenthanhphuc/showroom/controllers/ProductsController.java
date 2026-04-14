package com.nguyenthanhphuc.showroom.controllers;

import com.nguyenthanhphuc.showroom.services.ProductsService;
import com.nguyenthanhphuc.showroom.services.SettingsService;
import com.nguyenthanhphuc.showroom.services.UpdateProductResult;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class ProductsController {
    private final Auth auth;
    private final SettingsService settingsService;
    private final ProductsService productsService;

    public ProductsController(Auth auth, SettingsService settingsService, ProductsService productsService) {
        this.auth = auth;
        this.settingsService = settingsService;
        this.productsService = productsService;
    }

    @GetMapping("/add-product")
    public String addProductGet(HttpServletRequest request, Model model) {
        if (!auth.isUserLoggedIn(request)) {
            return "redirect:/login";
        }

        model.addAttribute("isUserLoggedIn", true)
                .addAttribute("companyName", settingsService.getCompanyName());
        return "add-product";
    }

    @PostMapping("/add-product")
    public String addProductPost(
            @RequestParam String productName,
            @RequestParam MultipartFile productImage,
            HttpServletRequest request,
            Model model
    ) {
        if (!auth.isUserLoggedIn(request)) {
            return "redirect:/login";
        }

        try {
            productsService.addProduct(productName, productImage);
            return "redirect:/#products";
        } catch (Exception e) {
            model.addAttribute("isUserLoggedIn", true)
                    .addAttribute("companyName", settingsService.getCompanyName())
                    .addAttribute("productName", productName)
                    .addAttribute("errorMsg", e.getMessage());
            return "add-product";
        }
    }

    @GetMapping("/edit-product")
    public String editProductGet(
            @RequestParam int id,
            HttpServletRequest request,
            Model model
    ) {
        if (!auth.isUserLoggedIn(request)) {
            return "redirect:/login";
        }

        var productName = productsService.getProductName(id);
        if (productName.isEmpty()) {
            return "redirect:/#products";
        }

        model.addAttribute("isUserLoggedIn", true)
                .addAttribute("companyName", settingsService.getCompanyName())
                .addAttribute("productId", id)
                .addAttribute("productName", productName.get());
        return "edit-product";
    }

    @PostMapping("/edit-product")
    public String editProductPost(
            @RequestParam int id,
            @RequestParam String productName,
            HttpServletRequest request,
            Model model
    ) {
        if (!auth.isUserLoggedIn(request)) {
            return "redirect:/login";
        }

        try {
            var result = productsService.updateProduct(id, productName);
            if (result == UpdateProductResult.OK) {
                return "redirect:/#products";
            }
            var errorMsg = switch (result) {
                case NAME_EXISTED -> "Property name must be unique";
                case NOT_FOUND -> "Property not found";
                default -> "Unable to update property";
            };
            model.addAttribute("isUserLoggedIn", true)
                    .addAttribute("companyName", settingsService.getCompanyName())
                    .addAttribute("productId", id)
                    .addAttribute("productName", productName)
                    .addAttribute("errorMsg", errorMsg);
            return "edit-product";
        } catch (Exception e) {
            model.addAttribute("isUserLoggedIn", true)
                    .addAttribute("companyName", settingsService.getCompanyName())
                    .addAttribute("productId", id)
                    .addAttribute("productName", productName)
                    .addAttribute("errorMsg", e.getMessage());
            return "edit-product";
        }
    }

    @GetMapping("/delete-product")
    public String deleteProduct(
            @RequestParam int id,
            HttpServletRequest request
    ) {
        if (!auth.isUserLoggedIn(request)) {
            return "redirect:/login";
        }

        productsService.deleteProduct(id);
        return "redirect:/#products";
    }
}
