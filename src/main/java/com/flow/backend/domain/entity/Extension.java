package com.flow.backend.domain.entity;

import com.flow.backend.domain.enums.ExtensionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "extensions",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_extensions_extension_type",
                columnNames = {"extension", "type"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Extension {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String extension;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ExtensionType type;

    @Column(nullable = false)
    private boolean checked;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private Extension(String extension, ExtensionType type, boolean checked) {
        this.extension = extension;
        this.type = type;
        this.checked = checked;
    }

    public static Extension fixed(String extension) {
        return new Extension(extension, ExtensionType.FIXED, false);
    }

    public static Extension custom(String extension) {
        return new Extension(extension, ExtensionType.CUSTOM, true);
    }

    public void updateChecked(boolean checked) {
        this.checked = checked;
    }

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
