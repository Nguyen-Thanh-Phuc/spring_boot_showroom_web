package com.nguyenthanhphuc.showroom.services;

import com.nguyenthanhphuc.showroom.models.GeneralInfo;
import org.springframework.stereotype.Service;

@Service
public class GeneralInfoService {
    private final Repo repo;

    public GeneralInfoService(Repo repo) {
        this.repo = repo;
    }

    public GeneralInfo getGeneralInfo() {
        return repo.getGeneralInfo();
    }

    public void updateGeneralInfo(String shortName, String longName, String featureImagePath) {
        repo.updateGeneralInfo(shortName, longName, featureImagePath);
    }
}
