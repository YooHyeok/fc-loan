package com.fc.loan.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public interface FileStorageService {
    void save(Long applicationId, MultipartFile[] files);
    Resource load(Long applicationId, String fileName);
    Resource loadAsZip(Long applicationId, String[] fileNames);
    Stream<Path> loadAll(Long applicationId);
    void deleteAll(Long applicationId);
}
