package com.nguyenthanhphuc.showroom.services;

import com.nguyenthanhphuc.showroom.utils.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductsService {
    private final Repo repo;
    private final Storage storage;

    public ProductsService(Repo repo, Storage storage) {
        this.repo = repo;
        this.storage = storage;
    }

    public List<Product> getProducts() {
        return repo.loadProducts();
    }

    public List<Product> searchProducts(String query) {
        var products = repo.loadProducts();
        if (StringUtils.isNullOrBlank(query)) {
            return products;
        }
        var trimmedQuery = query.trim().toLowerCase();
        return products.stream()
                .filter(product -> product.name() != null && product.name().toLowerCase().contains(trimmedQuery))
                .collect(Collectors.toList());
    }

    public Product getProductById(int id) {
        return repo.loadProductById(id);
    }

    public Optional<String> getProductName(int id) {
        if (id <= 0) {
            return Optional.empty();
        }
        return repo.loadProductName(id);
    }

    public void addProduct(String name, MultipartFile image) throws Exception {
        var trimmedName = validateProductName(name);
        validateProductImage(image, true);

        if (repo.existsProductName(trimmedName)) {
            throw new Exception("Property name must be unique");
        }

        var id = repo.insertProduct(trimmedName);
        storage.saveFile(image, "products/" + id + ".jpg");
    }

    public void updateProduct(int id, String name, MultipartFile image) throws Exception {
        if (id <= 0) {
            throw new Exception("Invalid property id");
        }
        var trimmedName = validateProductName(name);
        validateProductImage(image, false);

        if (repo.existsProductName(trimmedName, id)) {
            throw new Exception("Property name must be unique");
        }

        repo.updateProductName(id, trimmedName);
        if (image != null && !image.isEmpty()) {
            storage.saveFile(image, "products/" + id + ".jpg");
        }
    }

    public UpdateProductResult updateProduct(int id, String name) throws Exception {
        if (id <= 0) {
            return UpdateProductResult.NOT_FOUND;
        }
        var trimmedName = validateProductName(name);
        return repo.updateProduct(id, trimmedName);
    }

    public void deleteProduct(int id) {
        repo.deleteProduct(id);
        storage.deleteFile("products/" + id + ".jpg");
    }

    private String validateProductName(String name) throws Exception {
        if (StringUtils.isNullOrBlank(name)) {
            throw new Exception("Property name cannot be null or blank");
        }
        var trimmedName = name.trim();
        if (trimmedName.isBlank()) {
            throw new Exception("Property name cannot be null or blank");
        }
        return trimmedName;
    }

    private void validateProductImage(MultipartFile image, boolean required) throws Exception {
        if (image == null || image.isEmpty()) {
            if (required) {
                throw new Exception("Property image cannot be null or empty");
            }
            return;
        }
        var contentType = image.getContentType();
        if (!"image/jpeg".equals(contentType)) {
            throw new Exception("Property image must be in JPG format");
        }
        if (image.getSize() > 1024 * 1024) {
            throw new Exception("Property image cannot be bigger than 1MB");
        }
    }
}
