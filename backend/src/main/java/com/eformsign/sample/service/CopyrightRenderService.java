package com.eformsign.sample.service;

import com.eformsign.sample.entity.Copyright;
import com.eformsign.sample.entity.CopyrightType;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class CopyrightRenderService {

    public String render(Copyright c) {
        if (c == null) return null;

        String year = (c.getPublishDate() != null)
                ? DateTimeFormatter.ofPattern("yyyy").format(c.getPublishDate())
                : "";

        if (c.getType() == CopyrightType.PUBLIC_LICENSE) {
            return String.format(
                    "본 저작물은 %s에서 %s년에 작성하였으며, %s 에서 무료로 다운받으실 수 있습니다.",
                    c.getName(),
                    year,
                    c.getUrl() != null ? c.getUrl() : ""
            );
        } else {
            return String.format("© %s %s. All rights reserved.",
                    year, c.getName());
        }
    }

    public String extractYear(LocalDateTime dt) {
        if (dt == null) return null;
        return DateTimeFormatter.ofPattern("yyyy").format(dt);
    }

    public String formatDate(LocalDateTime dt) {
        if (dt == null) return null;
        return DateTimeFormatter.ofPattern("yyyy-MM-dd").format(dt);
    }
}