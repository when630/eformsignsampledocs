package com.eformsign.sample.repository;

import com.eformsign.sample.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByCategoryId(Long categoryId);
    Optional<Document> findById(Long id);
    List<Document> findByCategoryIdIn(List<Long> ids);
}