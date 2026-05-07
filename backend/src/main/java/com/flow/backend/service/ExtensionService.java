package com.flow.backend.service;

import com.flow.backend.domain.entity.Extension;
import com.flow.backend.domain.enums.ExtensionType;
import com.flow.backend.dto.request.CustomExtensionCreateRequest;
import com.flow.backend.dto.request.FixedExtensionUpdateRequest;
import com.flow.backend.dto.response.ExtensionListResponse;
import com.flow.backend.dto.response.ExtensionResponse;
import com.flow.backend.exception.ErrorCode;
import com.flow.backend.exception.ExtensionException;
import com.flow.backend.repository.ExtensionRepository;
import java.util.List;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExtensionService {

    private static final int MAX_EXTENSION_LENGTH = 20;
    private static final int MAX_CUSTOM_EXTENSION_COUNT = 200;
    private static final Pattern EXTENSION_PATTERN = Pattern.compile("^[a-z0-9]+$");

    private final ExtensionRepository extensionRepository;

    public ExtensionListResponse getExtensions() {
        return new ExtensionListResponse(getFixedExtensions(), getCustomExtensions());
    }

    public List<ExtensionResponse> getFixedExtensions() {
        return extensionRepository.findAllByTypeOrderByIdAsc(ExtensionType.FIXED)
                .stream()
                .map(ExtensionResponse::from)
                .toList();
    }

    public List<ExtensionResponse> getCustomExtensions() {
        return extensionRepository.findAllByTypeOrderByIdAsc(ExtensionType.CUSTOM)
                .stream()
                .map(ExtensionResponse::from)
                .toList();
    }

    @Transactional
    public ExtensionResponse updateFixedExtension(Long id, FixedExtensionUpdateRequest request) {
        Extension extension = extensionRepository.findByIdAndType(id, ExtensionType.FIXED)
                .orElseThrow(() -> new ExtensionException(ErrorCode.EXTENSION_NOT_FOUND));

        extension.updateChecked(request.checked());
        return ExtensionResponse.from(extension);
    }

    @Transactional
    public ExtensionResponse createCustomExtension(CustomExtensionCreateRequest request) {
        String normalizedExtension = ExtensionNormalizer.normalize(request.extension());
        validateExtension(normalizedExtension);

        if (extensionRepository.existsByExtension(normalizedExtension)) {
            throw new ExtensionException(ErrorCode.DUPLICATE_EXTENSION);
        }
        if (extensionRepository.countByType(ExtensionType.CUSTOM) >= MAX_CUSTOM_EXTENSION_COUNT) {
            throw new ExtensionException(ErrorCode.CUSTOM_EXTENSION_LIMIT_EXCEEDED);
        }

        Extension extension = extensionRepository.save(Extension.custom(normalizedExtension));
        return ExtensionResponse.from(extension);
    }

    @Transactional
    public void deleteCustomExtension(Long id) {
        Extension extension = extensionRepository.findById(id)
                .orElseThrow(() -> new ExtensionException(ErrorCode.EXTENSION_NOT_FOUND));

        if (extension.getType() == ExtensionType.FIXED) {
            throw new ExtensionException(ErrorCode.FIXED_EXTENSION_DELETE_NOT_ALLOWED);
        }

        extensionRepository.delete(extension);
    }

    private void validateExtension(String extension) {
        if (extension.isBlank()
                || extension.length() > MAX_EXTENSION_LENGTH
                || !EXTENSION_PATTERN.matcher(extension).matches()) {
            throw new ExtensionException(ErrorCode.INVALID_EXTENSION);
        }
    }

}
