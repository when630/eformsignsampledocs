package com.eformsign.sample.repository;

import com.eformsign.sample.entity.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByCategoryId(Long categoryId);
    Optional<Document> findById(Long id);
    List<Document> findByCategoryIdIn(List<Long> ids);

    Page<Document> findByTitleEqualsIgnoreCase(String q, Pageable pageable);
    Page<Document> findByTitleContainingIgnoreCase(String q, Pageable pageable);

    @Query(value = """
            SELECT * FROM document d
            WHERE LOWER(d.title) REGEXP CONCAT('[[:<:]]', LOWER(:q), '[[:>:]]')
            ORDER BY d.title ASC
            """,
            countQuery = """
            SELECT COUNT(*) FROM document d
            WHERE LOWER(d.title) REGEXP CONCAT('[[:<:]]', LOWER(:q), '[[:>:]]')
            """,
            nativeQuery = true)
    Page<Document> findByTitleWord(@Param("q") String q, Pageable pageable);

    // 카테고리(들) 기준으로 문서 + 저작권을 한 번에
    @Query("""
       select distinct d
         from Document d
         left join fetch d.copyright
        where d.categoryId in :categoryIds
       """)
    List<Document> findAllByCategoryIdInWithCopyright(@Param("categoryIds") List<Long> categoryIds);

    // 단건 조회용(서비스에서 쓰던 메서드)
    @Query("""
       select d
         from Document d
         left join fetch d.copyright
        where d.id = :id
       """)
    Optional<Document> findByIdWithCopyright(@Param("id") Long id);
}