package com.eformsign.sample.controller;

import com.eformsign.sample.dto.TemplateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/eformsign")
public class TemplateController {

    @PostMapping("/template-option")
    public Map<String, Object> generateTemplateOption(@RequestBody TemplateRequest request) throws IOException {
        // 1. PDF 파일을 base64로 인코딩
        byte[] fileBytes = Files.readAllBytes(Paths.get(request.getFilePath()));
        String base64File = Base64.getEncoder().encodeToString(fileBytes);

        // 2. 옵션 생성
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
                "type", "01", // 생성
                "template_type", "unstructured_form"
        ));

        option.put("layout", Map.of(
                "lang_code", "ko",
                "header", true,
                "footer", true
        ));

        // 수신자(step) 구성
        List<Map<String, Object>> recipients = request.getRecipients().stream()
                .map(id -> {
                    Map<String, Object> recipient = new HashMap<>();
                    recipient.put("id", id);
                    recipient.put("name", "참여자");
                    return recipient;
                })
                .collect(Collectors.toList());

        Map<String, Object> step = new HashMap<>();
        step.put("step_type", "05");
        step.put("step_name", "참여자");
        step.put("use_mail", true);
        step.put("use_sms", false);
        step.put("use_alimtalk", false);
        step.put("recipients", recipients);
        step.put("auth", Map.of(
                "valid", Map.of("day", "7", "hour", "0")
        ));

        option.put("prefill", Map.of(
                "template_name", request.getTemplateName(),
                "step_settings", List.of(step),
                "quick_processing", false
        ));

        option.put("template_file", Map.of(
                "name", "upload.pdf",
                "mime", "@file/octet-stream",
                "data", base64File
        ));

        return option;
    }
}