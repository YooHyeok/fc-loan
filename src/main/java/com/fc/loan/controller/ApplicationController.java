package com.fc.loan.controller;

import com.fc.loan.dto.FileDTO;
import com.fc.loan.dto.ResponseDTO;
import com.fc.loan.service.ApplicationService;
import com.fc.loan.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.fc.loan.dto.ApplicationDTO.*;


@RequiredArgsConstructor
@RestController
@RequestMapping("/applications")
public class ApplicationController extends AbstractController {

    private final ApplicationService applicationService;
    private final FileStorageService fileStorageService;

    @PostMapping
    public ResponseDTO<Response> create(@RequestBody Request request) {
        return ok(applicationService.create(request)); // AbstractController에 정의된 ok 메소드 호출
    }

    @GetMapping("/{applicationId}")
    public ResponseDTO<Response> create(@PathVariable Long applicationId) {
        return ok(applicationService.get(applicationId)); // AbstractController에 정의된 ok 메소드 호출
    }

    @PutMapping("/{applicationId}")
    public ResponseDTO<Response> update(@PathVariable Long applicationId, @RequestBody Request request) {
        return ok(applicationService.update(applicationId, request)); // AbstractController에 정의된 ok 메소드 호출
    }

    @DeleteMapping("/{applicationId}")
    public ResponseDTO<Response> delete(@PathVariable Long applicationId) {
        applicationService.delete(applicationId);
        return ok(); // AbstractController에 정의된 ok 메소드 호출
    }

    @PostMapping("/{applicationId}/terms")
    public ResponseDTO<Boolean> create(@PathVariable Long applicationId, @RequestBody AcceptTerms request) {
        return ok(applicationService.acceptTerms(applicationId, request)); // AbstractController에 정의된 ok 메소드 호출
    }

    /**
     * [입회 서류 서버 업로드] <br/>
     * 다중 파일을 서버의 upload 디렉토리 경로에 업로드 한다. (다중 파일)
     * @param files
     * @return
     */
    @PostMapping("/files")
    public ResponseDTO<Void> upload(MultipartFile[] files) {
        fileStorageService.save(files);
        return ok();
    }

    /**
     * [입회 서류 로컬 다운로드] <br/>
     * 서버의 upload 디렉토리 경로로 부터 파라미터로 넘겨받은 파일명에 대한 파일을 찾고
     * 클라이언트 로컬의 download 디렉토리 경로로 해당 파일을 다운로드 한다.
     * ResponseDTO에 header 설정에 대한 처리를 하지 않았으므로 ResponseEntity를 사용한다.
     * @param fileName
     * @return
     */
    @GetMapping("/files")
    public ResponseEntity<Resource> download(String fileName) {
        Resource file = fileStorageService.load(fileName);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }

    /**
     * [입회 서류 압축파일 로컬 다운로드] <br/>
     * 서버의 upload 디렉토리 경로로 부터 파라미터로 넘겨받은 파일명에 대한 파일들을 찾고
     * 클라이언트 로컬의 download 디렉토리 경로로 해당 파일들을 압축 다운로드 한다.
     * ResponseDTO에 header 설정에 대한 처리를 하지 않았으므로 ResponseEntity를 사용한다.
     * @param fileNames
     * @return
     */
    @GetMapping("/zipFiles")
    public ResponseEntity<Resource> downloads(String[] fileNames) {
        System.out.println("fileNames = " + Arrays.toString(fileNames));
        Resource file = fileStorageService.loadAsZip(fileNames);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }

    /**
     * [입회 서류 정보 전체 조회] <br/>
     * upload 디렉토리 경로에있는 모든 파일 정보를 읽어들인다. <br/>
     * {파일명, 다운로드 URL 정보}
     * @return
     */
    @GetMapping("/files/infos")
    public ResponseDTO<List<FileDTO>> getFileInfos() {
        return ok(fileStorageService.loadAll()
                .map(path -> {
                    /* 파일명 */
                    String filename = path.getFileName().toString();
                    /* 실제 서버로부터 다운로드 가능한 URL */
                    String resourceDownloadUrl =
                            MvcUriComponentsBuilder
                            .fromMethodName(ApplicationController.class, "download", filename)
                            .build()
                            .toString();
                    return FileDTO.builder()
                            .name(filename)
                            .url(resourceDownloadUrl)
                            .build();
                })
                .collect(Collectors.toList()));
    }

    @DeleteMapping("/files")
    public ResponseDTO<Void> deleteAll() {
        fileStorageService.deleteAll();
        return ok();
    }
}
