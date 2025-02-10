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

        try (PDDocument pdfDocument = new PDDocument()) {

            addImages(pdfDocument, pageScaleToImage, images);
            pdfDocument.save(".\\SlapAPdfOut-" + UUID.randomUUID() + ".pdf");
        }
    }

    void addImages(PDDocument pdDocument, float pageScaleToImage, List<BufferedImage> images) throws IOException {

        final Rectangle imageDimension = getPdfImageDimension(images);
        final float scaledWidth = ((float) imageDimension.getWidth()) * pageScaleToImage;
        final PDRectangle pdRectangle = new PDRectangle(
                scaledWidth, ((float) imageDimension.getHeight()) * pageScaleToImage);

        final File tempImageFile = new File(".\\" + UUID.randomUUID() + ".jpeg");
        final PDPage pdPage = new PDPage(pdRectangle);
        pdDocument.addPage(pdPage);

        float offsetY = pdPage.getMediaBox().getHeight();

        try (PDPageContentStream pdPageStream = new PDPageContentStream(pdDocument, pdPage)) {
            for (BufferedImage image : images) {

                float dynamicHeight = image.getHeight() * pageScaleToImage;

                if (ImageIO.write(image, "jpeg", tempImageFile)) {
                    PDImageXObject pdImage = PDImageXObject.createFromFileByContent(tempImageFile, pdDocument);
                    pdPageStream.drawImage(
                            pdImage,
                            0,
                            offsetY - dynamicHeight,
                            scaledWidth,
                            dynamicHeight);
                }
                offsetY -= dynamicHeight;
            }
        } finally {
            if (!tempImageFile.delete()) {
                System.out.println("Unable to delete temp image file");
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
