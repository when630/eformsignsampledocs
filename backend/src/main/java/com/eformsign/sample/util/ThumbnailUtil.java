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
    private static final File CONVERTED_PDF_DIR = new File("backend/data/converted_pdfs");

    static {
        if (!THUMBNAIL_DIR.exists()) {
            THUMBNAIL_DIR.mkdirs();
        }
        if (!CONVERTED_PDF_DIR.exists()) {
            CONVERTED_PDF_DIR.mkdirs();
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
     * DOC 썸네일 생성 (없으면 생성)
     */
    public static File generateDocThumbnail(File docFile) throws Exception {
        String baseName = getSafeBaseName(docFile.getName(), ".doc");
        File thumbnailFile = new File(THUMBNAIL_DIR, baseName + ".jpg");

        if (thumbnailFile.exists()) {
            log.info("이미 존재하는 DOC 썸네일 반환: {}", thumbnailFile.getAbsolutePath());
            return thumbnailFile;
        }

        // 변환된 PDF 파일 경로 (converted_pdfs 디렉토리)
        File pdfFile = new File(CONVERTED_PDF_DIR, baseName + ".pdf");

        // PDF 없으면 생성
        if (!pdfFile.exists()) {
            convertDocToPdf(docFile, pdfFile);
        } else {
            log.info("이미 존재하는 PDF 사용: {}", pdfFile.getAbsolutePath());
        }

        return generatePdfThumbnail(pdfFile);
    }

    /**
     * LibreOffice로 DOC → PDF 변환
     */
    public static void convertDocToPdf(File docFile, File outputPdf) throws IOException, InterruptedException {
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
            throw new FileNotFoundException("LibreOffice가 PDF 파일을 생성하지 못했습니다: " + convertedPdf.getAbsolutePath());
        }

        // rename → 지정한 outputPdf 이름으로 이동
        Files.move(convertedPdf.toPath(), outputPdf.toPath(), StandardCopyOption.REPLACE_EXISTING);
        log.info("DOC → PDF 변환 완료: {}", outputPdf.getAbsolutePath());
    }

    /**
     * 파일 이름에서 확장자 제거 및 안전한 이름 반환
     */
    private static String getSafeBaseName(String filename, String extension) {
        return filename.replaceAll(extension + "$", "").replaceAll("\\s+", "_");
    }

    public static File getOrConvertDocToPdf(File docFile, File outputPdf) throws IOException, InterruptedException {
        if (outputPdf.exists()) {
            return outputPdf;
        }

        ProcessBuilder pb = new ProcessBuilder(
                "libreoffice",
                "--headless",
                "--convert-to", "pdf",
                "--outdir", outputPdf.getParent(),
                docFile.getAbsolutePath()
        );
        pb.redirectErrorStream(true);
        Process process = pb.start();
        process.waitFor();

        if (!outputPdf.exists()) {
            throw new FileNotFoundException("PDF 변환 실패: " + outputPdf.getAbsolutePath());
        }
        return outputPdf;
    }
}