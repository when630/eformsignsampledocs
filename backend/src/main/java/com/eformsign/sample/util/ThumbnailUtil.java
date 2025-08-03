import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.convert.out.pdf.PdfConversion;
import org.docx4j.convert.out.pdf.viaXSLFO.Conversion;

import java.io.File;

public class ThumbnailUtil {

    public static File convertDocxToPdf(File docxFile) throws Docx4JException {
        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(docxFile);
        PdfConversion converter = new Conversion(wordMLPackage);

        File pdfFile = File.createTempFile("converted_", ".pdf");
        converter.output(pdfFile);
        return pdfFile;
    }

    public static File generateDocxThumbnail(File docxFile) throws Exception {
        File pdf = convertDocxToPdf(docxFile);
        return generatePdfThumbnail(pdf); // PDF → JPG (이미 구현되어 있다고 가정)
    }

    public static File generatePdfThumbnail(File pdfFile) throws IOException {
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFRenderer renderer = new PDFRenderer(document);
            BufferedImage image = renderer.renderImageWithDPI(0, 150);
            File output = File.createTempFile("thumb_", ".jpg");
            ImageIO.write(image, "jpg", output);
            return output;
        }
    }
}