package com.flow.backend.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.flow.backend.domain.enums.ExtensionType;
import org.junit.jupiter.api.Test;

class ExtensionTest {

    @Test
    void fixedExtensionIsCreatedUnchecked() {
        Extension extension = Extension.fixed("exe");

        assertThat(extension.getExtension()).isEqualTo("exe");
        assertThat(extension.getType()).isEqualTo(ExtensionType.FIXED);
        assertThat(extension.isChecked()).isFalse();
    }

    @Test
    void customExtensionIsCreatedChecked() {
        Extension extension = Extension.custom("zip");

        assertThat(extension.getExtension()).isEqualTo("zip");
        assertThat(extension.getType()).isEqualTo(ExtensionType.CUSTOM);
        assertThat(extension.isChecked()).isTrue();
    }

    @Test
    void updateCheckedChangesBlockState() {
        Extension extension = Extension.fixed("exe");

        extension.updateChecked(true);

        assertThat(extension.isChecked()).isTrue();
    }
}
