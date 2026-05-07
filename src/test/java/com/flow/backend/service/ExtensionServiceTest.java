package com.flow.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.flow.backend.domain.entity.Extension;
import com.flow.backend.domain.enums.ExtensionType;
import com.flow.backend.dto.request.CustomExtensionCreateRequest;
import com.flow.backend.dto.request.FixedExtensionUpdateRequest;
import com.flow.backend.dto.response.ExtensionResponse;
import com.flow.backend.exception.ErrorCode;
import com.flow.backend.exception.ExtensionException;
import com.flow.backend.repository.ExtensionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:extension-service-test;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ExtensionServiceTest {

    @Autowired
    private ExtensionService extensionService;

    @Autowired
    private ExtensionRepository extensionRepository;

    @BeforeEach
    void setUp() {
        extensionRepository.deleteAllInBatch();
    }

    @Test
    void createCustomExtensionNormalizesInputAndSavesChecked() {
        ExtensionResponse response = extensionService.createCustomExtension(
                new CustomExtensionCreateRequest(" .ZIP ")
        );

        assertThat(response.extension()).isEqualTo("zip");
        assertThat(response.type()).isEqualTo(ExtensionType.CUSTOM);
        assertThat(response.checked()).isTrue();
        assertThat(extensionRepository.existsByExtension("zip")).isTrue();
    }

    @Test
    void createCustomExtensionRejectsDuplicateAfterNormalization() {
        extensionRepository.save(Extension.custom("zip"));

        assertThatThrownBy(() -> extensionService.createCustomExtension(
                new CustomExtensionCreateRequest(".ZIP")
        ))
                .isInstanceOf(ExtensionException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DUPLICATE_EXTENSION);
    }

    @Test
    void createCustomExtensionRejectsDuplicateWithUncheckedFixedExtension() {
        extensionRepository.save(Extension.fixed("exe"));

        assertThatThrownBy(() -> extensionService.createCustomExtension(
                new CustomExtensionCreateRequest("EXE")
        ))
                .isInstanceOf(ExtensionException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DUPLICATE_EXTENSION);
    }

    @Test
    void createCustomExtensionRejectsDuplicateWithCheckedFixedExtension() {
        Extension fixed = extensionRepository.save(Extension.fixed("exe"));
        fixed.updateChecked(true);

        assertThatThrownBy(() -> extensionService.createCustomExtension(
                new CustomExtensionCreateRequest("EXE")
        ))
                .isInstanceOf(ExtensionException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DUPLICATE_EXTENSION);
    }

    @Test
    void createCustomExtensionRejectsInvalidInput() {
        assertThatThrownBy(() -> extensionService.createCustomExtension(
                new CustomExtensionCreateRequest("zip!")
        ))
                .isInstanceOf(ExtensionException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_EXTENSION);
    }

    @Test
    void createCustomExtensionRejectsMoreThanTwoHundredCustomExtensions() {
        for (int i = 0; i < 200; i++) {
            extensionRepository.save(Extension.custom("a" + i));
        }

        assertThatThrownBy(() -> extensionService.createCustomExtension(
                new CustomExtensionCreateRequest("over")
        ))
                .isInstanceOf(ExtensionException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CUSTOM_EXTENSION_LIMIT_EXCEEDED);
    }

    @Test
    void updateFixedExtensionChangesCheckedValue() {
        Extension fixed = extensionRepository.save(Extension.fixed("exe"));

        ExtensionResponse response = extensionService.updateFixedExtension(
                fixed.getId(),
                new FixedExtensionUpdateRequest(true)
        );

        assertThat(response.checked()).isTrue();
        assertThat(extensionRepository.findByExtensionAndType("exe", ExtensionType.FIXED))
                .hasValueSatisfying(extension -> assertThat(extension.isChecked()).isTrue());
    }

    @Test
    void updateFixedExtensionKeepsRowWhenUnchecked() {
        Extension fixed = extensionRepository.save(Extension.fixed("exe"));
        fixed.updateChecked(true);

        ExtensionResponse response = extensionService.updateFixedExtension(
                fixed.getId(),
                new FixedExtensionUpdateRequest(false)
        );

        assertThat(response.checked()).isFalse();
        assertThat(extensionRepository.findByExtensionAndType("exe", ExtensionType.FIXED))
                .hasValueSatisfying(extension -> assertThat(extension.isChecked()).isFalse());
    }

    @Test
    void updateFixedExtensionRejectsInvalidFixedOptionId() {
        assertThatThrownBy(() -> extensionService.updateFixedExtension(
                999L,
                new FixedExtensionUpdateRequest(true)
        ))
                .isInstanceOf(ExtensionException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.EXTENSION_NOT_FOUND);
    }

    @Test
    void updateFixedExtensionRejectsCustomExtensionId() {
        Extension custom = extensionRepository.save(Extension.custom("exe"));

        assertThatThrownBy(() -> extensionService.updateFixedExtension(
                custom.getId(),
                new FixedExtensionUpdateRequest(true)
        ))
                .isInstanceOf(ExtensionException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.EXTENSION_NOT_FOUND);
    }

    @Test
    void getFixedExtensionsReturnsFixedRowsFromDatabase() {
        Extension exe = extensionRepository.save(Extension.fixed("exe"));
        Extension dll = extensionRepository.save(Extension.fixed("dll"));
        extensionRepository.save(Extension.custom("zip"));

        assertThat(extensionService.getFixedExtensions())
                .extracting(ExtensionResponse::id, ExtensionResponse::extension)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(exe.getId(), "exe"),
                        org.assertj.core.groups.Tuple.tuple(dll.getId(), "dll")
                );
    }

    @Test
    void deleteCustomExtensionDeletesOnlyCustomExtension() {
        Extension custom = extensionRepository.save(Extension.custom("zip"));

        extensionService.deleteCustomExtension(custom.getId());

        assertThat(extensionRepository.findById(custom.getId())).isEmpty();
    }

    @Test
    void deleteCustomExtensionRejectsFixedExtension() {
        Extension fixed = extensionRepository.save(Extension.fixed("exe"));
        fixed.updateChecked(true);

        assertThatThrownBy(() -> extensionService.deleteCustomExtension(fixed.getId()))
                .isInstanceOf(ExtensionException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.FIXED_EXTENSION_DELETE_NOT_ALLOWED);
    }
}
