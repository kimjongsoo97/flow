package com.flow.backend.repository;

import com.flow.backend.domain.entity.Extension;
import com.flow.backend.domain.enums.ExtensionType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExtensionRepository extends JpaRepository<Extension, Long> {

    boolean existsByExtension(String extension);

    Optional<Extension> findByExtension(String extension);

    List<Extension> findAllByExtension(String extension);

    boolean existsByExtensionAndType(String extension, ExtensionType type);

    long countByType(ExtensionType type);

    List<Extension> findAllByTypeOrderByIdAsc(ExtensionType type);

    Optional<Extension> findByIdAndType(Long id, ExtensionType type);

    Optional<Extension> findByExtensionAndType(String extension, ExtensionType type);
}
