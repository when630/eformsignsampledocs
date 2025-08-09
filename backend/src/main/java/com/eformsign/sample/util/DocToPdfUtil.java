package com.eformsign.sample.util;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@Slf4j
public class DocToPdfUtil {

    private static final File PDF_DIR = new File("backend/data/converted_pdfs");

    static {
        if (!PDF_DIR.exists()) {
            PDF_DIR.mkdirs();
        }
    }

    /**
     * DOC 파일이 있을 경우 → PDF 경로 반환
     * 이미 PDF가 있으면 그걸 쓰고, 없으면 변환
     */
    public static File getOrConvertPdfFromDoc(File docFile) throws IOException, InterruptedException {
        String baseName = docFile.getName().replaceAll("\\.doc$", "").replaceAll("\\s+", "_");
        File pdfFile = new File(PDF_DIR, baseName + ".pdf");

        if (pdfFile.exists()) {
            log.info("이미 존재하는 PDF 사용: {}", pdfFile.getAbsolutePath());
            return pdfFile;
        }

        convertDocToPdf(docFile, pdfFile);
        return pdfFile;
    }

    /**
     * LibreOffice로 DOC → PDF 변환
     */
    private static void convertDocToPdf(File docFile, File outputPdf) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(
                "libreoffice",
                "--headless",
                "--convert-to", "pdf",
                "--outdir", outputPdf.getParent(),
                docFile.getAbsolutePath()
        );
        pb.redirectErrorStream(true);

        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                log.debug("[LibreOffice] {}", line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("LibreOffice PDF 변환 실패 (코드: " + exitCode + ")");
        }

        String convertedName = docFile.getName().replaceAll("\\.doc$", ".pdf");
        File convertedPdf = new File(outputPdf.getParentFile(), convertedName);

        if (!convertedPdf.exists()) {
            throw new FileNotFoundException("PDF 파일이 생성되지 않았습니다: " + convertedPdf.getAbsolutePath());
        }

        Files.move(convertedPdf.toPath(), outputPdf.toPath(), StandardCopyOption.REPLACE_EXISTING);
        log.info("DOC → PDF 변환 완료: {}", outputPdf.getAbsolutePath());
    }
}