package com.flow.backend.dto.response;

import com.flow.backend.domain.entity.Extension;
import com.flow.backend.domain.enums.ExtensionType;
import java.time.LocalDateTime;

public record ExtensionResponse(
        Long id,
        String extension,
        ExtensionType type,
        boolean checked,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static ExtensionResponse from(Extension extension) {
        return new ExtensionResponse(
                extension.getId(),
                extension.getExtension(),
                extension.getType(),
                extension.isChecked(),
                extension.getCreatedAt(),
                extension.getUpdatedAt()
        );
    }
}
