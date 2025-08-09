package com.eformsign.sample.service;

import com.eformsign.sample.dto.TemplateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.*;

@Service
@RequiredArgsConstructor
public class EformsignService {

    private String base64File;

    public Map<String, Object> createTemplateOption(TemplateRequest request) {
        Map<String, Object> option = new HashMap<>();

        option.put("company", Map.of(
                "id", request.getCompanyId(),
                "country_code", request.getCountryCode(),
                "user_key", request.getUserId()
        ));

        option.put("user", Map.of(
                "id", request.getUserId(),
                "access_token", request.getAccessToken(),
                "refresh_token", request.getRefreshToken()
        ));

        option.put("mode", Map.of(
                "type", "01", // 템플릿 생성
                "template_type", "form"
        ));

        option.put("layout", Map.of(
                "lang_code", "ko",
                "header", true,
                "footer", true
        ));

        Map<String, Object> step = new HashMap<>();
        step.put("step_type", "05");
        step.put("step_name", "참여자");
        step.put("use_mail", true);
        step.put("use_sms", false);
        step.put("use_alimtalk", false);
        step.put("recipients", new ArrayList<>()); // 수신자 없음
        step.put("auth", Map.of("valid", Map.of("day", "7", "hour", "0")));

        option.put("prefill", Map.of(
                "template_name", request.getTemplateName(),
                "step_settings", List.of(step),
                "quick_processing", false
        ));

        option.put("template_file", Map.of(
                "name", request.getTemplateName(),
                "mime", "@file/octet-stream",
                "data", request.getBase64File()
        ));

        return option;
    }
}