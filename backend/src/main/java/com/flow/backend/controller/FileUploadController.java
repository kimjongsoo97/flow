package com.flow.backend.controller;

import com.flow.backend.dto.response.FileUploadResponse;
import com.flow.backend.service.FileValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping({"/api/files", "/api/v1/files"})
public class FileUploadController {

    private final FileValidationService fileValidationService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileUploadResponse> upload(@RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(fileValidationService.validateUpload(file));
    }
}
