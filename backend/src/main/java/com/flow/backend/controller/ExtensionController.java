package com.flow.backend.controller;

import com.flow.backend.dto.request.CustomExtensionCreateRequest;
import com.flow.backend.dto.request.FixedExtensionUpdateRequest;
import com.flow.backend.dto.response.ExtensionListResponse;
import com.flow.backend.dto.response.ExtensionResponse;
import com.flow.backend.service.ExtensionService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/extensions")
public class ExtensionController {

    private final ExtensionService extensionService;

    @GetMapping
    public ResponseEntity<ExtensionListResponse> getExtensions() {
        return ResponseEntity.ok(extensionService.getExtensions());
    }

    @GetMapping("/fixed")
    public ResponseEntity<List<ExtensionResponse>> getFixedExtensions() {
        return ResponseEntity.ok(extensionService.getFixedExtensions());
    }

    @PatchMapping("/fixed/{id}")
    public ResponseEntity<ExtensionResponse> updateFixedExtension(
            @PathVariable Long id,
            @Valid @RequestBody FixedExtensionUpdateRequest request
    ) {
        return ResponseEntity.ok(extensionService.updateFixedExtension(id, request));
    }

    @GetMapping("/custom")
    public ResponseEntity<List<ExtensionResponse>> getCustomExtensions() {
        return ResponseEntity.ok(extensionService.getCustomExtensions());
    }

    @PostMapping("/custom")
    public ResponseEntity<ExtensionResponse> createCustomExtension(
            @Valid @RequestBody CustomExtensionCreateRequest request
    ) {
        ExtensionResponse response = extensionService.createCustomExtension(request);
        return ResponseEntity
                .created(URI.create("/api/v1/extensions/custom/" + response.id()))
                .body(response);
    }

    @DeleteMapping("/custom/{id}")
    public ResponseEntity<Void> deleteCustomExtension(@PathVariable Long id) {
        extensionService.deleteCustomExtension(id);
        return ResponseEntity.noContent().build();
    }
}
