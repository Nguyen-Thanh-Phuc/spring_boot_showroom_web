package com.nguyenthanhphuc.showroom.services;

import com.nguyenthanhphuc.showroom.models.GeneralInfo;

import java.util.List;
import java.util.Optional;

public interface Repo {
    boolean checkLogin(String username, String password);

    CompanyName loadCompanyName();

    void saveCompanyName(CompanyName companyName);

    String loadAboutContent();

    GeneralInfo getGeneralInfo();

    void updateGeneralInfo(String shortName, String longName, String featureImagePath);

    void saveAboutContent(String aboutContent);

    List<Product> loadProducts();

    Product loadProductById(int id);

    Optional<String> loadProductName(int id);

    int insertProduct(String name);

    void updateProductName(int id, String name);

    UpdateProductResult updateProduct(int id, String name);

    void deleteProduct(int id);

    boolean existsProductName(String name);

    boolean existsProductName(String name, int excludeId);
}
