package com.fc.loan.service;

import com.fc.loan.exception.BaseException;
import com.fc.loan.exception.ResultType;
import com.fc.loan.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {

    private final ApplicationRepository applicationRepository;

    @Value("${spring.servlet.multipart.location}")
    private String uploadPath;

    @Override
    public void save(Long applicationId, MultipartFile[] files) {
        if(!isPresentApplication(applicationId)) throw new BaseException(ResultType.SYSTEM_ERROR);
        /* 전달받은 MultipartFile객체를 읽고 업로드하고자 하는 지정된 경로에 업로드(복사) 한다. */
        try {
            String applicationPath = uploadPath.concat("/"+applicationId);
            /* 디렉토리가 존재하지 않는다면 생성해준다. */
            Path directoryPath = Path.of(applicationPath);
//            Path directoryPath = Paths.get(applicationPath); // 사용 가능

            if (!Files.exists(directoryPath)) {
                Files.createDirectory(directoryPath); // 상위 디렉토리(upload 포함 상위)가 존재하지 않을 경우 NoSuchFileException 발생
//                Files.createDirectories(directoryPath); // 상위 디렉토리가 존재하지 않을 경우 상위를 포함하여 디렉토리를 모두 생성함. - 접근권한 없을 경우 AccessDeninedException발생
            }

            /* 파일 업로드 시작 */
            for (MultipartFile file : files) {

                /**
                 * FileOutputStream - 파일에 바이트 데이터를 쓰기 위한 스트림 <br/>
                 * 파일을 열고 쓰는 과정에서 더 세부적인 제어가 가능 <br/>
                 * (파일의 특정 위치에 데이터 쓰기 가능) <br/>
                 * 직접적 처리 필요(파일 업로드(복사)시 모든 데이터 수동 처리 <br/>
                 * 버퍼링 지원 부족 (대용량 파일 처리시 직접 버퍼링 구현)
                 */
                File targetFile = new File(applicationPath, file.getOriginalFilename());
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
                        Paths.get(applicationPath).resolve(file.getOriginalFilename()),
                        StandardCopyOption.REPLACE_EXISTING // 기존 파일 존재시 덮어 쓴다.
                );*/

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
//            throw new BaseException(ResultType.SYSTEM_ERROR);
        }
    }

    @Override
    public Resource load(Long applicationId, String fileName) {
        if(!isPresentApplication(applicationId)) throw new BaseException(ResultType.SYSTEM_ERROR);

        UrlResource resource = null;// toUri 절대경로를 통해 파일시스템으로 부터 리소스를 받아온다.
        try {
            String applicationPath = uploadPath.concat("/"+applicationId);

            Path file = Paths.get(applicationPath).resolve(fileName);
            resource = new UrlResource(file.toUri());
            if (resource.isReadable() || resource.exists()) return resource;
            throw new BaseException(ResultType.NOT_EXIST);
        } catch (Exception e) {
            throw new BaseException(ResultType.SYSTEM_ERROR);
        }

    }

    @Override
    public Resource loadAsZip(Long applicationId, String[] fileNames) {

        try {
            // 압축 파일을 생성하기 위한 임시 파일
//            Path zipFile = Files.createTempFile("attachZipFiles", ".zip");
            Path zipFile = Paths.get("attachZipFiles.zip");

            // 파일들을 압축
            try (ZipOutputStream zipOut = new ZipOutputStream(Files.newOutputStream(zipFile))) {
                for (String fileName : fileNames) {
                    Resource file = load(applicationId, fileName);
                    if (file.exists() && file.isReadable()) {
                        ZipEntry zipEntry = new ZipEntry(file.getFilename());
                        zipOut.putNextEntry(zipEntry);
                        Files.copy(file.getFile().toPath(), zipOut);
                        zipOut.closeEntry();
                    }
                }
            }
            // 압축된 파일을 리소스로 로드
            System.out.println("zipFile.toUri().toString() = " + zipFile.toUri().toString());
            return new UrlResource(zipFile.toUri());

        } catch (IOException e) {
            throw new BaseException(ResultType.SYSTEM_ERROR);
        }
    }

    @Override
    public Stream<Path> loadAll(Long applicationId) {
        if(!isPresentApplication(applicationId)) throw new BaseException(ResultType.SYSTEM_ERROR);

        try {
            String applicationPath = uploadPath.concat("/"+applicationId);

            /* walk : Paths 경로에 해당하는 모든 경로를 탐색 - 함께 전달받은 Depth에 해당하는 파일들을 탐색해서 반환하는 기능 제공 */
            return Files.walk(Paths.get(applicationPath), 1) // uploadPath의 1Depth에 해당하는 경로만 탐색
                    .peek(System.out::println)
                    .filter(path -> !path.equals(Paths.get(uploadPath))); // 파일만 반환해주기 위해 uploadPath의 하위 파일들만 조회하게 된다.
        } catch (Exception e) {
            throw new BaseException(ResultType.SYSTEM_ERROR);
        }
    }

    @Override
    public void deleteAll(Long applicationId) {
        if(!isPresentApplication(applicationId)) throw new BaseException(ResultType.SYSTEM_ERROR);
        try {
            String applicationPath = uploadPath.concat("/" + applicationId);

            FileSystemUtils.deleteRecursively(Paths.get(applicationPath).toFile()); // uploadPath 경로에 존재하는 모든 파일을 삭제한다.
        } catch (Exception e) {
            throw new BaseException(ResultType.SYSTEM_ERROR);

        }
    }

    private boolean isPresentApplication(Long applicationId) {
        return applicationRepository.existsById(applicationId);
    }
}
