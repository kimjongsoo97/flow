package com.flow.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.flow.backend.domain.entity.Extension;
import com.flow.backend.dto.response.FileUploadResponse;
import com.flow.backend.exception.ErrorCode;
import com.flow.backend.exception.ExtensionException;
import com.flow.backend.repository.ExtensionRepository;
import com.flow.backend.repository.FileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:file-validation-service-test;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class FileValidationServiceTest {

    @Autowired
    private FileValidationService fileValidationService;

    @Autowired
    private ExtensionRepository extensionRepository;

    @Autowired
    private FileRepository fileRepository;

    @BeforeEach
    void setUp() {
        fileRepository.deleteAllInBatch();
        extensionRepository.deleteAllInBatch();
    }

    @Test
    void validateUploadBlocksCheckedFixedExtension() {
        Extension extension = extensionRepository.save(Extension.fixed("exe"));
        extension.updateChecked(true);
        MockMultipartFile file = new MockMultipartFile("file", "TEST.EXE", "application/octet-stream", "test".getBytes());

        FileUploadResponse response = fileValidationService.validateUpload(file);

        assertThat(response.allowed()).isFalse();
        assertThat(response.extension()).isEqualTo("exe");
        assertThat(response.originalFilename()).isEqualTo("TEST.EXE");
        assertThat(response.message()).isEqualTo("차단된 확장자입니다.");
        assertThat(fileRepository.count()).isZero();
    }

    @Test
    void validateUploadBlocksCustomExtension() {
        extensionRepository.save(Extension.custom("gz"));
        MockMultipartFile file = new MockMultipartFile("file", "archive.tar.gz", "application/gzip", "test".getBytes());

        FileUploadResponse response = fileValidationService.validateUpload(file);

        assertThat(response.allowed()).isFalse();
        assertThat(response.extension()).isEqualTo("gz");
    }

    @Test
    void validateUploadBlocksCustomExtensionEvenWhenUncheckedFixedExtensionExists() {
        extensionRepository.save(Extension.fixed("exe"));
        extensionRepository.save(Extension.custom("exe"));
        MockMultipartFile file = new MockMultipartFile("file", "TEST.EXE", "application/octet-stream", "test".getBytes());

        FileUploadResponse response = fileValidationService.validateUpload(file);

        assertThat(response.allowed()).isFalse();
        assertThat(response.extension()).isEqualTo("exe");
        assertThat(fileRepository.count()).isZero();
    }

    @Test
    void validateUploadAllowsUnknownExtension() {
        MockMultipartFile file = new MockMultipartFile("file", "test.png", "image/png", "test".getBytes());

        FileUploadResponse response = fileValidationService.validateUpload(file);

        assertThat(response.allowed()).isTrue();
        assertThat(response.fileId()).isNotNull();
        assertThat(response.extension()).isEqualTo("png");
        assertThat(response.message()).isEqualTo("업로드 가능한 확장자입니다.");
        assertThat(fileRepository.findAll())
                .singleElement()
                .satisfies(savedFile -> {
                    assertThat(savedFile.getId()).isEqualTo(response.fileId());
                    assertThat(savedFile.getFilename()).isEqualTo("test.png");
                    assertThat(savedFile.getExtension()).isEqualTo("png");
                });
    }

    @Test
    void deleteUploadedFileDeletesSavedFile() {
        MockMultipartFile file = new MockMultipartFile("file", "test.png", "image/png", "test".getBytes());
        FileUploadResponse response = fileValidationService.validateUpload(file);

        fileValidationService.deleteUploadedFile(response.fileId());

        assertThat(fileRepository.findById(response.fileId())).isEmpty();
    }

    @Test
    void validateUploadRejectsEmptyFile() {
        MockMultipartFile file = new MockMultipartFile("file", "test.png", "image/png", new byte[0]);

        assertThatThrownBy(() -> fileValidationService.validateUpload(file))
                .isInstanceOf(ExtensionException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.EMPTY_FILE);
        assertThat(fileRepository.count()).isZero();
    }

    @Test
    void validateUploadRejectsMissingFilename() {
        MockMultipartFile file = new MockMultipartFile("file", "", "image/png", "test".getBytes());

        assertThatThrownBy(() -> fileValidationService.validateUpload(file))
                .isInstanceOf(ExtensionException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.MISSING_FILENAME);
    }

    @Test
    void validateUploadRejectsMissingExtension() {
        MockMultipartFile file = new MockMultipartFile("file", "test", "application/octet-stream", "test".getBytes());

        assertThatThrownBy(() -> fileValidationService.validateUpload(file))
                .isInstanceOf(ExtensionException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.MISSING_EXTENSION);
    }
}
