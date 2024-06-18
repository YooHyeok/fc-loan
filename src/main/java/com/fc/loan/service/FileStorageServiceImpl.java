package com.fc.loan.service;

import com.fc.loan.exception.BaseException;
import com.fc.loan.exception.ResultType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${spring.servlet.multipart.location}")
    private String uploadPath;

    @Override
    public void save(MultipartFile[] files) {
        /* 전달받은 MultipartFile객체를 읽고 업로드하고자 하는 지정된 경로에 업로드(복사) 한다. */
        try {
            for (MultipartFile file : files) {

                /**
                 * FileOutputStream - 파일에 바이트 데이터를 쓰기 위한 스트림 <br/>
                 * 파일을 열고 쓰는 과정에서 더 세부적인 제어가 가능 <br/>
                 * (파일의 특정 위치에 데이터 쓰기 가능) <br/>
                 * 직접적 처리 필요(파일 업로드(복사)시 모든 데이터 수동 처리 <br/>
                 * 버퍼링 지원 부족 (대용량 파일 처리시 직접 버퍼링 구현)
                 */
                File targetFile = new File(uploadPath, file.getOriginalFilename());
                try (OutputStream os = new FileOutputStream(targetFile)) {
                    os.write(file.getBytes());
                }

                /**
                 * 파일 복사 작업 수행 유틸리티 메소드 <br/>
                 * 간결한 코드 한줄로 파일 업로드(복사)작업 수행 가능 <br/>
                 * 내부적으로 효율적인 버퍼링 및 I/O 처리 구현 <br/>
                 * 기존 파일 덮어 쓸지 여부 등 다양한 옵션 제공 <br/>
                 * 파일 복사 과정의 세부 제어 어려움 <br/>
                 * 내부 동작이 추상화 되어 있어 디버깅이 어려움 <br/>
                 * 간단한 파일 복사/이동시 사용
                 */
                /*Files.copy(
                        file.getInputStream(),
                        Paths.get(uploadPath).resolve(file.getOriginalFilename()),
                        StandardCopyOption.REPLACE_EXISTING // 기존 파일 존재시 덮어 쓴다.
                );*/

            }
        } catch (IOException e) {
            throw new BaseException(ResultType.SYSTEM_ERROR);
        }
    }

    @Override
    public Resource load(String fileName) {

        Path file = Paths.get(uploadPath).resolve(fileName);
        UrlResource resource = null;// toUri 절대경로를 통해 파일시스템으로 부터 리소스를 받아온다.
        try {
            resource = new UrlResource(file.toUri());
            if (resource.isReadable() || resource.exists()) return resource;
            throw new BaseException(ResultType.NOT_EXIST);
        } catch (Exception e) {
            throw new BaseException(ResultType.SYSTEM_ERROR);
        }

    }
}
