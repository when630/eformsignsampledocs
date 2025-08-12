// src/main/java/com/eformsign/sample/config/SpaResourceConfig.java
package com.eformsign.sample.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

@Configuration
public class SpaResourceConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        // 요청한 정적 파일이 실제로 있으면 그대로 제공
                        Resource requested = location.createRelative(resourcePath);
                        if (requested.exists() && requested.isReadable()) {
                            return requested;
                        }
                        // /api 로 시작하면 SPA가 아니라 컨트롤러/필터 체인에 맡김
                        if (resourcePath.startsWith("api")) {
                            return null;
                        }
                        // 나머지는 모두 index.html로 폴백 → SPA 라우팅 처리
                        return location.createRelative("index.html");
                    }
                });
    }
}