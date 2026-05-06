package com.flow.backend.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CustomExtensionCreateRequest(
        @NotBlank(message = "확장자는 필수입니다.")
        String extension
) {
}
