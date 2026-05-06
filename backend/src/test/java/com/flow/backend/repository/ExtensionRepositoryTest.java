package com.flow.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.flow.backend.domain.entity.Extension;
import com.flow.backend.domain.enums.ExtensionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
class ExtensionRepositoryTest {

    @Autowired
    private ExtensionRepository extensionRepository;

    @Test
    void existsByExtensionReturnsTrueWhenExtensionExists() {
        extensionRepository.save(Extension.fixed("exe"));

        boolean exists = extensionRepository.existsByExtension("exe");

        assertThat(exists).isTrue();
    }

    @Test
    void countByTypeCountsOnlyRequestedType() {
        extensionRepository.save(Extension.fixed("exe"));
        extensionRepository.save(Extension.custom("zip"));
        extensionRepository.save(Extension.custom("rar"));

        long customCount = extensionRepository.countByType(ExtensionType.CUSTOM);

        assertThat(customCount).isEqualTo(2);
    }

    @Test
    void findAllByTypeOrderByIdAscReturnsOnlyRequestedType() {
        Extension fixed = extensionRepository.save(Extension.fixed("exe"));
        Extension firstCustom = extensionRepository.save(Extension.custom("zip"));
        Extension secondCustom = extensionRepository.save(Extension.custom("rar"));

        assertThat(extensionRepository.findAllByTypeOrderByIdAsc(ExtensionType.CUSTOM))
                .containsExactly(firstCustom, secondCustom)
                .doesNotContain(fixed);
    }

    @Test
    void findByIdAndTypeReturnsEmptyWhenTypeDoesNotMatch() {
        Extension fixed = extensionRepository.save(Extension.fixed("exe"));

        assertThat(extensionRepository.findByIdAndType(fixed.getId(), ExtensionType.CUSTOM))
                .isEmpty();
    }
}
