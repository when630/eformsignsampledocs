package com.eformsign.sample.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class CopyrightTypeConverter implements AttributeConverter<CopyrightType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(CopyrightType attribute) {
        return attribute == null ? null : attribute.getCode();
    }

    @Override
    public CopyrightType convertToEntityAttribute(Integer dbData) {
        return CopyrightType.fromCode(dbData);
    }
}