package com.flow.backend.config;

import com.flow.backend.domain.entity.Extension;
import com.flow.backend.repository.ExtensionRepository;
import com.flow.backend.service.ExtensionNormalizer;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class FixedExtensionInitializer implements ApplicationRunner {

    private final ExtensionRepository extensionRepository;

    @Value("${app.fixed-extensions.defaults:exe,sh,bat,cmd,com,scr,js}")
    private List<String> defaultFixedExtensions;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        defaultFixedExtensions.stream()
                .map(ExtensionNormalizer::normalize)
                .filter(extension -> !extension.isBlank())
                .filter(extension -> !extensionRepository.existsByExtension(extension))
                .forEach(extension -> extensionRepository.save(Extension.fixed(extension)));
    }
}
