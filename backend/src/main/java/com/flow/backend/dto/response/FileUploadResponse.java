package com.flow.backend.dto.response;

public record FileUploadResponse(
        boolean allowed,
        String extension,
        String originalFilename,
        String message
) {
}
