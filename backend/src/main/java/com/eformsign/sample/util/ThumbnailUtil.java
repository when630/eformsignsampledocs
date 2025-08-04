package com.eformsign.sample.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.docx4j.Docx4J;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.fonts.IdentityPlusMapper;
import org.docx4j.fonts.Mapper;
import org.docx4j.fonts.PhysicalFonts;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

@Slf4j
public class ThumbnailUtil {

    public static File generatePdfThumbnail(File pdfFile) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            PDFRenderer renderer = new PDFRenderer(document);
            BufferedImage image = renderer.renderImageWithDPI(0, 150); // 첫 페이지 150DPI
            File output = File.createTempFile("thumb_", ".jpg");
            ImageIO.write(image, "jpg", output);
            return output;
        }
    }

    public static File generateDocxThumbnail(File docxFile) throws Exception {
        File pdf = convertDocxToPdf(docxFile);
        return generatePdfThumbnail(pdf);
    }

    public static File convertDocxToPdf(File docxFile) throws Exception {
        File outputPdf = File.createTempFile("converted_", ".pdf");

        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(docxFile);

        // 폰트 매퍼 수동 설정
        Mapper fontMapper = new IdentityPlusMapper();  // 여기서 오류가 났었음
        // 폰트 이름 필터도 필요시 설정 가능:
        // PhysicalFonts.setRegex(".*(Arial|MalgunGothic|Nanum).*");
        wordMLPackage.setFontMapper(fontMapper);

        FOSettings foSettings = Docx4J.createFOSettings();
        foSettings.setWmlPackage(wordMLPackage);

        try (OutputStream os = new FileOutputStream(outputPdf)) {
            Docx4J.toFO(foSettings, os, Docx4J.FLAG_EXPORT_PREFER_XSL);
        }

        if (!outputPdf.exists()) {
            throw new IOException("PDF 파일 생성 실패");
        }

        return outputPdf;
    }
}