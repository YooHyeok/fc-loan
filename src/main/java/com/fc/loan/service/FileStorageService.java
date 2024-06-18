package com.fc.loan.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileStorageService {
    void save(MultipartFile[] files);
    Resource load(String fileName);
}
