package com.flow.backend.service;

import com.flow.backend.domain.entity.Extension;
import com.flow.backend.domain.entity.File;
import com.flow.backend.domain.enums.ExtensionType;
import com.flow.backend.dto.response.FileUploadResponse;
import com.flow.backend.exception.ErrorCode;
import com.flow.backend.exception.ExtensionException;
import com.flow.backend.repository.ExtensionRepository;
import com.flow.backend.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FileValidationService {

    private static final String ALLOWED_MESSAGE = "업로드 가능한 확장자입니다.";
    private static final String BLOCKED_MESSAGE = "차단된 확장자입니다.";

    private final ExtensionRepository extensionRepository;
    private final FileRepository fileRepository;

    @Transactional
    public FileUploadResponse validateUpload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ExtensionException(ErrorCode.EMPTY_FILE);
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new ExtensionException(ErrorCode.MISSING_FILENAME);
        }

        String extension = extractExtension(originalFilename);
        boolean blocked = extensionRepository.findByExtension(extension)
                .map(this::isBlocked)
                .orElse(false);
        String message = blocked ? BLOCKED_MESSAGE : ALLOWED_MESSAGE;

        Long fileId = null;
        if (!blocked) {
            File savedFile = fileRepository.save(File.of(originalFilename, extension));
            fileId = savedFile.getId();
        }

        return new FileUploadResponse(fileId, !blocked, extension, originalFilename, message);
    }

    @Transactional
    public void deleteUploadedFile(Long id) {
        if (!fileRepository.existsById(id)) {
            throw new ExtensionException(ErrorCode.FILE_NOT_FOUND);
        }

        fileRepository.deleteById(id);
    }

    private String extractExtension(String originalFilename) {
        int lastDotIndex = originalFilename.lastIndexOf('.');
        if (lastDotIndex < 0 || lastDotIndex == originalFilename.length() - 1) {
            throw new ExtensionException(ErrorCode.MISSING_EXTENSION);
        }

        String extension = ExtensionNormalizer.normalize(originalFilename.substring(lastDotIndex + 1));
        if (extension.isBlank()) {
            throw new ExtensionException(ErrorCode.MISSING_EXTENSION);
        }
        return extension;
    }

    private boolean isBlocked(Extension extension) {
        return extension.getType() == ExtensionType.CUSTOM
                || (extension.getType() == ExtensionType.FIXED && extension.isChecked());
    }
}
