package com.eformsign.sample.entity;

import lombok.Getter;

@Getter
public enum CopyrightType {
    PUBLIC_LICENSE(1, "공공누리"),
    GENERAL(2, "일반");

    private final int code;
    private final String label;

    CopyrightType(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static CopyrightType fromCode(Integer code) {
        if (code == null) return null;
        for (var t : values()) {
            if (t.code == code) return t;
        }
        throw new IllegalArgumentException("Unknown code: " + code);
    }
}