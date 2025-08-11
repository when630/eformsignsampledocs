// CategoryRepository.java
package com.eformsign.sample.repository;

import com.eformsign.sample.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findAllByName(String name);
}