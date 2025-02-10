package md.slapapdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode.APPEND;

public class PdfMaker {

    public void createPdfFromImage(BufferedImage bufferedImage, float pageScaleToImage) throws IOException {

        final File tempImageFile = new File(".\\" + UUID.randomUUID() + ".jpeg");
        try (PDDocument pdfDoc = new PDDocument()) {

            if (ImageIO.write(bufferedImage, "jpeg", tempImageFile)) {
                addImage(pdfDoc, tempImageFile, pageScaleToImage);
                pdfDoc.save(".\\SlapAPdfOut-" + UUID.randomUUID() + ".pdf");
            }

        } finally {
            if (!tempImageFile.delete()) {
                System.out.println("Unable to delete temp image file");
            }
        }
    }

    void addImage(PDDocument pdfDocument, File tempImageFile, float scaleDownRatio) throws IOException {

        final PDImageXObject pdImage = PDImageXObject.createFromFileByContent(tempImageFile, pdfDocument);
        final PDPage pdPage = new PDPage(new PDRectangle(pdImage.getWidth() * scaleDownRatio, pdImage.getHeight() * scaleDownRatio));
        final PDRectangle pageSize = pdPage.getMediaBox();
        pdfDocument.addPage(pdPage);

        try (PDPageContentStream pdPageStream = new PDPageContentStream(pdfDocument, pdPage)) {
            pdPageStream.drawImage(pdImage, 0, 0, pageSize.getWidth(), pageSize.getHeight());
        }
    }

    public void createPdfFromImages(List<BufferedImage> images, float pageScaleToImage) throws IOException {

        final File tempImageFile = new File(".\\" + UUID.randomUUID() + ".jpeg");
        try (PDDocument pdfDoc = new PDDocument()) {

            addImages(pdfDoc, tempImageFile, pageScaleToImage, images);
            pdfDoc.save(".\\SlapAPdfOut-" + UUID.randomUUID() + ".pdf");

        } finally {
            if (!tempImageFile.delete()) {
                System.out.println("Unable to delete temp image file");
            }
        }
    }

    void addImages(PDDocument pdfDoc,
                   File tempImageFile,
                   float pageScaleToImage,
                   List<BufferedImage> images) throws IOException {

        Rectangle imageDimension = getPdfImageDimension(images);
        final PDRectangle pdRectangle = new PDRectangle(
                (float) (imageDimension.getWidth() * pageScaleToImage),
                (float) (imageDimension.getHeight() * pageScaleToImage));

        final PDPage pdPage = new PDPage(pdRectangle);
        pdfDoc.addPage(pdPage);
        float offsetY = pdPage.getMediaBox().getHeight();

        try (PDPageContentStream pdPageStream = new PDPageContentStream(pdfDoc, pdPage, APPEND, false)) {
            for (BufferedImage image : images) {

                float dynamicHeight = image.getHeight() * pageScaleToImage;

                if (ImageIO.write(image, "jpeg", tempImageFile)) {
                    PDImageXObject pdImage = PDImageXObject.createFromFileByContent(tempImageFile, pdfDoc);
                    pdPageStream.drawImage(
                            pdImage,
                            0,
                            offsetY - dynamicHeight,
                            image.getWidth() * pageScaleToImage,
                            dynamicHeight);
                }
                offsetY -= dynamicHeight;
            }
        }
    }

    public Rectangle getPdfImageDimension(List<BufferedImage> images) {

        int height = 0;
        int width = 0;

        for (BufferedImage image : images) {
            height += image.getHeight();
            if (width < image.getWidth()) {
                width = image.getWidth();
            }
        }
        return new Rectangle(width, height);
    }

}
