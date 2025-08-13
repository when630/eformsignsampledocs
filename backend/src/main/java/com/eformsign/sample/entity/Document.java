package com.eformsign.sample.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 분류 ID (추후 Category 엔티티로 변경 가능) */
    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    /** 저장소 ID (추후 Storage 엔티티로 변경 가능) */
    @Column(name = "storage_id", nullable = false)
    private Long storageId;

    /** 저작권 FK (NULL 허용: 저작권 표시 불필요 문서 대비) */
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "copyright_id",
            foreignKey = @ForeignKey(ConstraintMode.CONSTRAINT))
    private Copyright copyright;

    /** 문서명 */
    @Column(nullable = false, length = 255)
    private String title;

    private LocalDateTime createdAt;
}