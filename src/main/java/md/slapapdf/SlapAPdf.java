package md.slapapdf;

import org.openqa.selenium.Dimension;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.List;

public class SlapAPdf {

    private static final Dimension DEFAULT_RESOLUTION_OVERRIDE = new Dimension(1200, 1080);
    private static final float DEFAULT_SCALE_OVERRIDE = 1.4f;
    private static final float DEFAULT_PDF_SCALE_OVERRIDE = 0.4f;

    // TODO: this is just a DEMO
    public static void main(String... urls) {

        if (urls == null || urls.length == 0 || urls[0] == null) {
            System.out.println("empty arguments");
            return;
        }
        try (final DriverWrapper driver = new DriverWrapper(DEFAULT_RESOLUTION_OVERRIDE)) {

            final ScreenshotMaker screenshotMaker = new ScreenshotMaker();
            final PdfMaker pdfMaker = new PdfMaker();

            System.out.println("Started processing, don't touch browser while scanning. Or modify this app to run headless");
            for (String url : urls) {

                if (!isValid(url)) continue;

                System.out.println("Scanning " + url);
                List<BufferedImage> images = screenshotMaker.screenshotAll(driver, url, DEFAULT_SCALE_OVERRIDE);
                if (images == null || images.isEmpty()) {
                    System.out.println("No valid data taken from page for PDF file creation");
                    continue;
                }

                pdfMaker.createPdfFromImages(images, DEFAULT_PDF_SCALE_OVERRIDE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println("DONE");
    }

    static boolean isValid(String url) {
        boolean isValid = true;
        try {
            if (url == null || url.isBlank()) isValid = false;
            else new URL(url);
        } catch (Exception ex) {
            isValid = false;
        }
        if (!isValid) System.out.println("Invalid URL " + url);
        return isValid;
    }

}