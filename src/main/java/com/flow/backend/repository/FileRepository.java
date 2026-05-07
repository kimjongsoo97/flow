package com.flow.backend.repository;

import com.flow.backend.domain.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<File, Long> {

    long countByExtension(String extension);
}
