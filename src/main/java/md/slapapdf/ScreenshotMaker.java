package md.slapapdf;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static md.slapapdf.DriverWrapper.*;

class ScreenshotMaker {

    private static final float DEFAULT_SCALE = 1;

    List<BufferedImage> screenshotAll(DriverWrapper driver,
                                      String url) throws IOException, InterruptedException {
        return screenshotAll(driver, url, DEFAULT_SCALE);
    }

    // TODO: Adjust/use dynamic wait times without thread sleep based on connections speed and override?
    List<BufferedImage> screenshotAll(DriverWrapper driver,
                                      String url,
                                      float scale) throws IOException, InterruptedException {

        driver.loadUrl(url).implicitWait(Duration.ofSeconds(3));
        Thread.sleep(2100);
        formatPageForScreenshots(driver, scale);
        Thread.sleep(1100);

        int pageHeight = driver.getPageHeight();
        int lastYOffset = 0;
        int yOffset = 0;
        int crop = 0;

        List<File> tempImageFiles = new ArrayList<>();
        while ((pageHeight > 0) && (yOffset >= 0) && (yOffset != pageHeight)) {

            tempImageFiles.add(driver.takeScreenshot());
            driver.scrollPageDown().implicitWait(Duration.ofSeconds(7));
            Thread.sleep(910);

            yOffset = driver.getYOffset();
            if (lastYOffset == yOffset) {
                break;
            } else {
                crop = yOffset - lastYOffset;
                lastYOffset = yOffset;
            }
            if (yOffset == pageHeight) {
                tempImageFiles.add(driver.takeScreenshot());
                Thread.sleep(910);
            }
        }

        return convertFilesToBufferedImages(tempImageFiles, crop);
    }

    List<BufferedImage> convertFilesToBufferedImages(List<File> tempImageFiles,
                                                     int lastImageCrop) throws IOException {

        List<BufferedImage> bufferedImageFiles = new ArrayList<>();
        for (int index = 0; index < tempImageFiles.size(); index++) {

            File tempImageFile = tempImageFiles.get(index);
            BufferedImage image = ImageIO.read(tempImageFile);

            // if last portion of pagedown screenshot is smaller due to scroll size and page height diff
            if ((index == tempImageFiles.size() - 1)) {
                if (!((lastImageCrop > 0) && (image.getHeight() > lastImageCrop))) {
                    System.out.println("Crop failed");
                } else {
                    image = image.getSubimage(
                            0,
                            image.getHeight() - lastImageCrop,
                            image.getWidth(),
                            lastImageCrop);
                }
            }
            bufferedImageFiles.add(image);
            tempImageFile.delete();
        }
        return bufferedImageFiles;
    }

    /* TODO: Pause animations at the correct time on complex pages
     * FIXME: Pages with parallax break when hiding fixed popups  */
    void formatPageForScreenshots(DriverWrapper driver, double scale) {
        driver.executeJs(HIDE_SCROLLBAR_BODY_JS);
        driver.executeJs(HIDE_SCROLLBAR_ELE_JS);
        driver.executeJs(String.format(HIDE_TAGS_JS, "nav"));
        driver.executeJs(String.format(HIDE_TAGS_JS, "header"));
        driver.executeJs(String.format(HIDE_FIXED_JS, "div"));
        driver.executeJs(String.format(HIDE_FIXED_JS, "section"));
        driver.executeJs(String.format(ZOOM_JS, scale));
        driver.implicitWait(Duration.ofSeconds(7));
        driver.executeJs(SCROLL_TOP_JS);
        driver.implicitWait(Duration.ofSeconds(5));
    }

}
