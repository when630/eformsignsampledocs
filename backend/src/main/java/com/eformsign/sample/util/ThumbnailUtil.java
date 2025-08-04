package com.eformsign.sample.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@Slf4j
public class ThumbnailUtil {

    private static final File THUMBNAIL_DIR = new File("backend/data/thumbnails");

    static {
        if (!THUMBNAIL_DIR.exists()) {
            THUMBNAIL_DIR.mkdirs();
        }
    }

    /**
     * PDF 썸네일 생성 (없으면 만들고, 있으면 반환)
     */
    public static File generatePdfThumbnail(File pdfFile) throws IOException {
        String baseName = getSafeBaseName(pdfFile.getName(), ".pdf");
        File output = new File(THUMBNAIL_DIR, baseName + ".jpg");

        if (output.exists()) {
            log.info("이미 존재하는 PDF 썸네일 반환: {}", output.getAbsolutePath());
            return output;
        }

        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            PDFRenderer renderer = new PDFRenderer(document);
            BufferedImage image = renderer.renderImageWithDPI(0, 150);
            ImageIO.write(image, "jpg", output);
            log.info("새 PDF 썸네일 생성 완료: {}", output.getAbsolutePath());
            return output;
        }
    }

    /**
     * DOCX 썸네일 생성 (없으면 생성)
     */
    public static File generateDocxThumbnail(File docxFile) throws Exception {
        String baseName = getSafeBaseName(docxFile.getName(), ".docx");
        File thumbnailFile = new File(THUMBNAIL_DIR, baseName + ".jpg");

        if (thumbnailFile.exists()) {
            log.info("이미 존재하는 DOCX 썸네일 반환: {}", thumbnailFile.getAbsolutePath());
            return thumbnailFile;
        }

        // 문서명 기반 PDF 경로 생성 (중복 방지)
        File pdfFile = new File(docxFile.getParentFile(), baseName + ".pdf");

        // PDF 없으면 생성
        if (!pdfFile.exists()) {
            convertDocxToPdf(docxFile, pdfFile);
        } else {
            log.info("이미 존재하는 PDF 사용: {}", pdfFile.getAbsolutePath());
        }

        return generatePdfThumbnail(pdfFile);
    }

    /**
     * LibreOffice로 DOCX → PDF 변환
     */
    public static void convertDocxToPdf(File docxFile, File outputPdf) throws IOException, InterruptedException {
        String libreOfficePath = "libreoffice"; // 시스템 PATH에 등록되어 있는 경우
        ProcessBuilder pb = new ProcessBuilder(
                libreOfficePath,
                "--headless",
                "--convert-to", "pdf",
                "--outdir", outputPdf.getParent(),
                docxFile.getAbsolutePath()
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

        String convertedName = docxFile.getName().replaceAll("\\.docx$", ".pdf");
        File convertedPdf = new File(outputPdf.getParentFile(), convertedName);

        if (!convertedPdf.exists()) {
            throw new FileNotFoundException("LibreOffice가 PDF 파일을 생성하지 못했습니다: " + convertedPdf.getAbsolutePath());
        }

        Files.move(convertedPdf.toPath(), outputPdf.toPath(), StandardCopyOption.REPLACE_EXISTING);
        log.info("DOCX → PDF 변환 완료: {}", outputPdf.getAbsolutePath());
    }

    /**
     * 파일 이름에서 확장자 제거 및 안전한 이름 반환
     */
    private static String getSafeBaseName(String filename, String extension) {
        return filename.replaceAll(extension + "$", "").replaceAll("\\s+", "_");
    }
}