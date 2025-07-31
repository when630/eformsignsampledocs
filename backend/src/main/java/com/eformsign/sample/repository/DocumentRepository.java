package com.eformsign.sample.repository;

import com.eformsign.sample.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByCategoryId(Long categoryId);
}