package com.eformsign.sample.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "download_log")
public class DownloadLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String documentId;

    @Column(nullable = false)
    private Long accountId;

    @Column(length = 45) // IPv6까지 고려
    private String ipAddress;

    @Column(nullable = false)
    private LocalDateTime downloadedAt;

    public DownloadLog() {
        // 기본 생성자
    }

    public DownloadLog(String documentId, Long accountId, String ipAddress) {
        this.documentId = documentId;
        this.accountId = accountId;
        this.ipAddress = ipAddress;
        this.downloadedAt = LocalDateTime.now();
    }

    // Getter & Setter (Lombok 사용 시 @Getter, @Setter 또는 @Data 사용 가능)
    public Long getId() {
        return id;
    }

    public String getDocumentId() {
        return documentId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public LocalDateTime getDownloadedAt() {
        return downloadedAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void setDownloadedAt(LocalDateTime downloadedAt) {
        this.downloadedAt = downloadedAt;
    }
}