package com.flow.backend.dto.response;

import java.util.List;

public record ExtensionListResponse(
        List<ExtensionResponse> fixed,
        List<ExtensionResponse> custom
) {
}
