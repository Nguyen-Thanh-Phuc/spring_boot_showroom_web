package com.nguyenthanhphuc.showroom.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LogoutController {
    @GetMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        return ResponseEntity.status(302)
                .header("Clear-Site-Data", "\"cookies\"")
                .header("Location", request.getContextPath() + "/")
                .build();
    }
}
