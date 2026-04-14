package com.nguyenthanhphuc.showroom.repositories;

import com.nguyenthanhphuc.showroom.services.Storage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class MyStorage implements Storage {
    @Value("${uploadsFolder}")
    private String uploadsFolder;

    @Override
    public void saveFile(MultipartFile file, String path) {
        var target = Path.of(uploadsFolder).resolve(path);
        try {
            Files.createDirectories(target.getParent());
            file.transferTo(target);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteFile(String path) {
        var target = Path.of(uploadsFolder).resolve(path);
        try {
            Files.deleteIfExists(target);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
