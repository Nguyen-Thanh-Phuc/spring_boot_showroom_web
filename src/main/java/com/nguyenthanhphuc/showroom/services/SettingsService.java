package com.nguyenthanhphuc.showroom.services;

import com.nguyenthanhphuc.showroom.utils.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class SettingsService {
    private final Repo repo;
    private final Storage storage;

    public SettingsService(Repo repo, Storage storage) {
        this.repo = repo;
        this.storage = storage;
    }

    public CompanyName getCompanyName() {
        return repo.loadCompanyName();
    }

    public CompanyName loadCompanyName() {
        return getCompanyName();
    }

    public String getAboutContent() {
        return repo.loadAboutContent();
    }

    public void updateAboutContent(String aboutContent) throws Exception {
        if (StringUtils.isNullOrBlank(aboutContent)) {
            throw new Exception("About content cannot be null or blank");
        }
        repo.saveAboutContent(aboutContent.trim().replace("\r\n", "\n"));
    }

    public void updateGeneralInfo(CompanyName companyName, MultipartFile featureImage) throws Exception {
        if (StringUtils.isNullOrBlank(companyName.shortName())) {
            throw new Exception("Company short name cannot be null or blank");
        }
        if (StringUtils.isNullOrBlank(companyName.longName())) {
            throw new Exception("Company long name cannot be null or blank");
        }
        if (featureImage != null && !featureImage.isEmpty()) {
            var contentType = featureImage.getContentType();
            if (!"image/jpeg".equals(contentType)) {
                throw new Exception("Feature image must be in JPG format");
            }
            if (featureImage.getSize() > 2 * 1024 * 1024) {
                throw new Exception("Feature image cannot be bigger than 2MB");
            }
        }

        repo.saveCompanyName(companyName);
        if (featureImage != null && !featureImage.isEmpty()) {
            storage.saveFile(featureImage, "feature.jpg");
        }
    }
}
