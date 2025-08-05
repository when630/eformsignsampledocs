package com.eformsign.sample.service;

import com.eformsign.sample.entity.DownloadLog;
import com.eformsign.sample.repository.DownloadLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

@Service
public class DownloadLogService {

    private final DownloadLogRepository downloadLogRepository;

    public DownloadLogService(DownloadLogRepository downloadLogRepository) {
        this.downloadLogRepository = downloadLogRepository;
    }

    public void logDownload(String documentId, Long accountId, HttpServletRequest request) {
        String ipAddress = getClientIp(request);
        DownloadLog log = new DownloadLog(documentId, accountId, ipAddress);
        downloadLogRepository.save(log);
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0]; // 프록시 체인 고려
        }
        return request.getRemoteAddr();
    }
}