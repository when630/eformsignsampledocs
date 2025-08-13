package com.eformsign.sample.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "copyright",
        indexes = {
                @Index(name = "ix_copyright_name", columnList = "name"),
                @Index(name = "ix_copyright_type", columnList = "type")
        }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Copyright {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 작성기관 */
    @Column(nullable = false, length = 255)
    private String name;

    /** 작성자(없으면 name으로 세팅) */
    @Column(name = "uploader_name", length = 255)
    private String uploaderName;

    /** 유형: 1=공공누리, 2=일반 */
    @Convert(converter = CopyrightTypeConverter.class)
    @Column(nullable = false)
    private CopyrightType type;

    /** 저작권 관련 URL */
    @Column(length = 512)
    private String url;

    /** 발행연도(또는 발행일) - DATETIME 사용 */
    @Column(name = "date")
    private LocalDateTime publishDate;

    @PrePersist
    @PreUpdate
    private void fillDefaults() {
        if (uploaderName == null || uploaderName.isBlank()) {
            uploaderName = name;
        }
    }
}
