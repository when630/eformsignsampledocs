// CategoryClosureRepository.java
package com.eformsign.sample.repository;

import com.eformsign.sample.entity.CategoryClosure;
import com.eformsign.sample.entity.CategoryClosureId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryClosureRepository extends JpaRepository<CategoryClosure, Long> {

    boolean existsByAncestorAndDescendant(Long ancestor, Long descendant);
}