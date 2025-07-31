// DocumentRepository.java
package com.eformsign.sample.repository;

import com.eformsign.sample.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, Long> {
}