package com.flow.backend.dto.request;

import jakarta.validation.constraints.NotNull;

public record FixedExtensionUpdateRequest(
        @NotNull(message = "checked 값은 필수입니다.")
        Boolean checked
) {
}
