package com.sacco.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class FileStorageService {
    private final Path baseDir;

    public FileStorageService(@Value("${app.storageDir:uploads}") String storageDir) {
        this.baseDir = Path.of(storageDir);
        try {
            Files.createDirectories(baseDir);
        } catch (Exception ignored) {
        }
    }

    public String saveMemberPhoto(String externalId, MultipartFile file) {
        try {
            String fileName = "member-" + externalId + "-" + file.getOriginalFilename();
            Path target = baseDir.resolve(fileName);
            file.transferTo(target.toFile());
            return target.toAbsolutePath().toString();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to store file", ex);
        }
    }
}
