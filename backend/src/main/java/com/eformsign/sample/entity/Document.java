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

    private Long categoryId;

    @Column(name = "storage_id")
    private Long storageId;

    private String uploaderName;

    private String title;

    private LocalDateTime createdAt;
}