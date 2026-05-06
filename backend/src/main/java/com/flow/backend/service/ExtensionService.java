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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    private static final List<String> DEFAULT_FIXED_EXTENSIONS = List.of(
            "exe", "sh", "bat", "cmd", "com", "scr", "js"
    );
    private static final Map<Long, String> FIXED_EXTENSION_OPTIONS = createFixedExtensionOptions();

    private final ExtensionRepository extensionRepository;

    public ExtensionListResponse getExtensions() {
        return new ExtensionListResponse(getFixedExtensions(), getCustomExtensions());
    }

    public List<ExtensionResponse> getFixedExtensions() {
        List<String> checkedFixedExtensions = extensionRepository.findAllByTypeOrderByIdAsc(ExtensionType.FIXED)
                .stream()
                .filter(Extension::isChecked)
                .map(Extension::getExtension)
                .toList();

        return FIXED_EXTENSION_OPTIONS.entrySet()
                .stream()
                .map(entry -> ExtensionResponse.fixedOption(
                        entry.getKey(),
                        entry.getValue(),
                        checkedFixedExtensions.contains(entry.getValue())
                ))
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
        String fixedExtension = findFixedExtensionByOptionId(id);
        Optional<Extension> savedExtension = extensionRepository.findByExtensionAndType(
                fixedExtension,
                ExtensionType.FIXED
        );

        if (request.checked()) {
            if (savedExtension.isEmpty() && extensionRepository.existsByExtension(fixedExtension)) {
                throw new ExtensionException(ErrorCode.DUPLICATE_EXTENSION);
            }
            savedExtension.ifPresentOrElse(
                    extension -> extension.updateChecked(true),
                    () -> extensionRepository.save(Extension.fixed(fixedExtension)).updateChecked(true)
            );
            return ExtensionResponse.fixedOption(id, fixedExtension, true);
        }

        savedExtension.ifPresent(extensionRepository::delete);
        return ExtensionResponse.fixedOption(id, fixedExtension, false);
    }

    @Transactional
    public ExtensionResponse createCustomExtension(CustomExtensionCreateRequest request) {
        String normalizedExtension = normalize(request.extension());
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

    private String normalize(String extension) {
        if (extension == null) {
            return "";
        }
        String normalized = extension.trim().toLowerCase();
        while (normalized.startsWith(".")) {
            normalized = normalized.substring(1);
        }
        return normalized;
    }

    private void validateExtension(String extension) {
        if (extension.isBlank()
                || extension.length() > MAX_EXTENSION_LENGTH
                || !EXTENSION_PATTERN.matcher(extension).matches()) {
            throw new ExtensionException(ErrorCode.INVALID_EXTENSION);
        }
    }

    private String findFixedExtensionByOptionId(Long id) {
        String fixedExtension = FIXED_EXTENSION_OPTIONS.get(id);
        if (fixedExtension == null) {
            throw new ExtensionException(ErrorCode.EXTENSION_NOT_FOUND);
        }
        return fixedExtension;
    }

    private static Map<Long, String> createFixedExtensionOptions() {
        Map<Long, String> options = new LinkedHashMap<>();
        for (int index = 0; index < DEFAULT_FIXED_EXTENSIONS.size(); index++) {
            options.put((long) index + 1, DEFAULT_FIXED_EXTENSIONS.get(index));
        }
        return options;
    }
}
