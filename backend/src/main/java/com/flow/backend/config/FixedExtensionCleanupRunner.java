package com.flow.backend.config;

import com.flow.backend.domain.enums.ExtensionType;
import com.flow.backend.repository.ExtensionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class FixedExtensionCleanupRunner implements ApplicationRunner {

    private final ExtensionRepository extensionRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        extensionRepository.deleteAllByTypeAndCheckedFalse(ExtensionType.FIXED);
    }
}
