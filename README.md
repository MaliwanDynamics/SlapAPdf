Create single page PDFs from web pages demo learning project 

Article on how it works [here](https://maliwandynamics.com/blog/software/2025/02/10/web-page-to-pdf/)

```java
try (final DriverWrapper driver = new DriverWrapper()) {

  PdfMaker pdfMaker = new PdfMaker();
  ScreenshotMaker screenshotMaker = new ScreenshotMaker();
  List<BufferedImage> images = screenshotMaker.screenshotAll(driver, url, DEFAULT_SCALE_OVERRIDE);
  pdfMaker.createPdfFromImages(images);

} catch (Exception ex) {
    ...
}
```
