package com.eformsign.sample.service;

import com.eformsign.sample.dto.CopyrightView;
import com.eformsign.sample.entity.Document;
import com.eformsign.sample.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DocumentReadService {
    private final DocumentRepository documentRepository;
    private final CopyrightRenderService renderService;

    public CopyrightView getCopyrightView(Long documentId) {
        Document doc = documentRepository.findByIdWithCopyright(documentId)
                .orElseThrow(() -> new IllegalArgumentException("문서를 찾을 수 없습니다. id=" + documentId));

        if (doc.getCopyright() == null) {
            return CopyrightView.builder()
                    .copyrightId(null)
                    .type(null)
                    .name(null)
                    .uploaderName(null)
                    .url(null)
                    .published(null)
                    .displayText(null) // 프런트에선 null이면 미표시
                    .build();
        }

        var c = doc.getCopyright();
        return CopyrightView.builder()
                .copyrightId(c.getId())
                .type(c.getType() == null ? null : c.getType().name())
                .name(c.getName())
                .uploaderName(c.getUploaderName())
                .url(c.getUrl())
                .published(renderService.formatDate(c.getPublishDate())) // "yyyy-MM-dd" 포맷
                .displayText(renderService.render(c))                    // 최종 표기 문구
                .build();
    }
}