package com.flow.backend.dto.response;

public record FileUploadResponse(
        Long fileId,
        boolean allowed,
        String extension,
        String originalFilename,
        String message
) {
}
