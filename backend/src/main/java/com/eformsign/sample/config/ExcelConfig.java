// src/main/java/com/eformsign/sample/config/ExcelConfig.java
package com.eformsign.sample.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExcelConfig {

    @Bean
    public ExcelReader excelReader() {
        return new ExcelReader();
    }
}