package com.eformsign.sample.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.fop.apps.FopFactory;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.docx4j.Docx4J;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.fonts.IdentityPlusMapper;
import org.docx4j.fonts.Mapper;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Slf4j
public class ThumbnailUtil {

    public static File generatePdfThumbnail(File pdfFile) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            PDFRenderer renderer = new PDFRenderer(document);
            BufferedImage image = renderer.renderImageWithDPI(0, 150); // 첫 페이지
            File output = File.createTempFile("thumb_", ".jpg");
            ImageIO.write(image, "jpg", output);
            return output;
        }
    }

    public static File generateDocxThumbnail(File docxFile) throws Exception {
        // fop.xconf 로드
        InputStream configStream = ThumbnailUtil.class.getClassLoader().getResourceAsStream("fop.xconf");
        if (configStream == null) {
            throw new FileNotFoundException("fop.xconf not found in resources");
        }

        // 임시 파일로 복사
        Path tempConfigPath = Files.createTempFile("fop", ".xconf");
        Files.copy(configStream, tempConfigPath, StandardCopyOption.REPLACE_EXISTING);

        File pdfFile = File.createTempFile("converted_", ".pdf");
        convertDocxToPdf(docxFile, tempConfigPath.toFile(), pdfFile);

        return generatePdfThumbnail(pdfFile);
    }

    public static void convertDocxToPdf(File docxFile, File configFile, File pdfFile) throws Exception {
        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(docxFile);

        // 한글 폰트 매퍼
        Mapper fontMapper = new IdentityPlusMapper();
        wordMLPackage.setFontMapper(fontMapper);

        FOSettings foSettings = Docx4J.createFOSettings();
        foSettings.setWmlPackage(wordMLPackage);

        // FopFactory 생성 (주의: InputStream 사용)
        try (InputStream configInput = new FileInputStream(configFile)) {
            FopFactory fopFactory = FopFactory.newInstance(new File(".").toURI(), configInput);
            foSettings.getSettings().put("fopFactory", fopFactory);
        }

        // PDF 생성
        try (OutputStream os = new FileOutputStream(pdfFile)) {
            Docx4J.toFO(foSettings, os, Docx4J.FLAG_EXPORT_PREFER_XSL);
        }
    }
}