package com.eformsign.sample.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CopyrightView {
    private Long copyrightId;     // null 가능
    private String type;          // "PUBLIC_LICENSE" | "GENERAL" | null
    private String name;          // 작성기관
    private String uploaderName;  // 작성자 (없으면 name과 동일)
    private String url;           // 저작권 관련 URL
    private String published;     // "2025" 또는 "2025-01-01" 등 표시용
    private String displayText;   // 최종 표기 문자열 (렌더링 결과)
}