package com.nguyenthanhphuc.showroom.services;

import org.springframework.stereotype.Service;

@Service
public class LoginService {
    private final Repo repo;

    public LoginService(Repo repo) {
        this.repo = repo;
    }

    public boolean checkLogin(String username, String password) {
        return repo.checkLogin(username, password);
    }
}
