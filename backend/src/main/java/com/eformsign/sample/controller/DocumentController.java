package com.eformsign.sample.controller;

import com.eformsign.sample.dto.TreeResponse;
import com.eformsign.sample.entity.Document;
import com.eformsign.sample.entity.Storage;
import com.eformsign.sample.repository.DocumentRepository;
import com.eformsign.sample.repository.StorageRepository;

import com.eformsign.sample.service.CategoryService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.MalformedURLException;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;

// ... (기존 import 생략)
import java.util.concurrent.TimeUnit;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/documents")
@Slf4j
public class DocumentController {

    private final CategoryService categoryService;
    private final DocumentRepository documentRepository;
    private final StorageRepository storageRepository;

    @GetMapping("/tree")
    public List<TreeResponse> getCategoryTree() {
        return categoryService.getCategoryTree();
    }

    @GetMapping("/by-category/{categoryId}")
    public List<Document> getDocumentsByCategory(@PathVariable Long categoryId) {
        return documentRepository.findByCategoryId(categoryId);
    }

    @GetMapping("/{id}")
    public Document getDocumentById(@PathVariable Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 문서 없음: id=" + id));
    }

    @GetMapping("/thumbnail/{id}")
    public ResponseEntity<Resource> getThumbnail(@PathVariable Long id) throws Exception {
        Storage storage = storageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("storage 없음"));

        File file = new File(storage.getPath());
        if (!file.exists()) {
            throw new IllegalArgumentException("파일 없음: " + storage.getPath());
        }

        String extension = getExtension(file.getName()).toLowerCase();

        File imageFile;
        switch (extension) {
            case "pdf":
                imageFile = generatePdfThumbnail(file);
                break;
            case "docx":
                imageFile = generateDocxThumbnail(file);
                break;
            default:
                throw new IllegalArgumentException("지원하지 않는 파일 형식: " + extension);
        }

        UrlResource resource = new UrlResource(imageFile.toURI());
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(resource);
    }

    private String getExtension(String filename) {
        int dotIndex = filename.lastIndexOf(".");
        return (dotIndex == -1) ? "" : filename.substring(dotIndex + 1);
    }

    private File generatePdfThumbnail(File pdfFile) throws IOException {
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFRenderer renderer = new PDFRenderer(document);
            BufferedImage image = renderer.renderImageWithDPI(0, 150);
            File output = File.createTempFile("thumb_", ".jpg");
            ImageIO.write(image, "jpg", output);
            return output;
        }
    }

    private File generateDocxThumbnail(File docxFile) throws Exception {
        File pdfFile = convertDocxToPdf(docxFile);
        return generatePdfThumbnail(pdfFile);
    }

    private File convertDocxToPdf(File docxFile) throws IOException, InterruptedException {
        File outputPdf = new File(docxFile.getParent(), removeExtension(docxFile.getName()) + ".pdf");

        ProcessBuilder pb = new ProcessBuilder(
                "soffice",
                "--headless",
                "--convert-to", "pdf",
                "--outdir", docxFile.getParent(),
                docxFile.getAbsolutePath()
        );

        pb.redirectErrorStream(true);
        Process process = pb.start();
        boolean finished = process.waitFor(10, TimeUnit.SECONDS);
        if (!finished || !outputPdf.exists()) {
            throw new IOException("DOCX to PDF 변환 실패");
        }

        return outputPdf;
    }

    private String removeExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return (dot == -1) ? fileName : fileName.substring(0, dot);
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) throws MalformedURLException {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 문서 없음: id=" + id));

        String filePath = storageRepository.findById(document.getStorageId())
                .orElseThrow(() -> new IllegalArgumentException("Storage 없음: id=" + document.getStorageId()))
                .getPath();

        Resource resource = new UrlResource(Paths.get(filePath).toUri());

        String fileName = document.getTitle() + ".docx"; // 필요에 따라 확장자 조정
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}