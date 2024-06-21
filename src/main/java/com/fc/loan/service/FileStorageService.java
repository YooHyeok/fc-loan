package com.fc.loan.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public interface FileStorageService {
    void save(MultipartFile[] files);
    Resource load(String fileName);
    Resource loadAsZip(String[] fileNames);
    Stream<Path> loadAll();
    void deleteAll();
}
