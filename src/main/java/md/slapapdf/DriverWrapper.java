package md.slapapdf;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.File;
import java.time.Duration;
import java.util.Collections;

// Temp object wrapper in case I switch away from selenium and/or chrome, change in one location.
class DriverWrapper implements AutoCloseable {

    static final String GET_SCROLLED_Y_OFFSET_JS = "return window.pageYOffset + window.innerHeight;";
    static final String GET_SCROLL_HEIGHT_JS = "return document.body.scrollHeight;";
    static final String SCROLL_PAGE_HEIGHT_JS = "window.scrollTo(window.pageYOffset, window.pageYOffset + window.innerHeight);";
    static final String SCROLL_TOP_JS = "window.scrollTo(0, 0);";

    static final String ZOOM_JS = "document.body.style.zoom = '%s';";

    static final String HIDE_SCROLLBAR_BODY_JS = "document.body.style.overflow = 'hidden';";
    static final String HIDE_SCROLLBAR_ELE_JS = "document.documentElement.style.overflow = 'hidden';";

    static final String HIDE_TAGS_JS = """
            const elements = document.getElementsByTagName("%s");
            for(var i = 0; i < elements.length; i++){
                elements[i].style.visibility = 'hidden';
                elements[i].style.display = 'none';
                elements[i].style.height = 0;
            };""";

    static final String HIDE_FIXED_JS = """
            const elements = document.querySelectorAll('%s');
            elements.forEach((element) => {
                if (window.getComputedStyle(element).position === 'fixed') {
                    element.style.visibility = 'hidden';
                    element.style.display = 'none';
                    element.style.height = 0;
                }
            });""";

    private final WebDriver driver;
    private final JavascriptExecutor jsExecutor;
    private final TakesScreenshot takesScreenshot;

    DriverWrapper() {
        this(new Dimension(1920, 1080));
    }

    DriverWrapper(Dimension resolution) {
        ChromeOptions options = new ChromeOptions()
                .setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"))
                .setExperimentalOption("useAutomationExtension", false)
                .addArguments(
                        "--incognito",
                        "--no-sandbox",
                        "--disable-blink-features=AutomationControlled",
                        "--disable-dev-shm-usage",
                        "--disable-extensions",
                        "--disable-popup-blocking",
                        "--disable-smooth-scrolling",
                        "disable-infobars"
                );
        driver = new ChromeDriver(options);
        // FIXME: At the moment its better to abandon the process if it takes this long to load single page.
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
        driver.manage().window().setSize(resolution);

        jsExecutor = (JavascriptExecutor) driver;
        takesScreenshot = (TakesScreenshot) driver;
    }

    int getPageHeight() {
        Object data = executeJs(GET_SCROLL_HEIGHT_JS);
        return convertToInt(data);
    }

    int getYOffset() {
        Object data = jsExecutor.executeScript(GET_SCROLLED_Y_OFFSET_JS);
        return convertToInt(data);
    }

    DriverWrapper scrollPageDown() {
        executeJs(SCROLL_PAGE_HEIGHT_JS);
        return this;
    }

    Object executeJs(String javaScript) {
        return jsExecutor.executeScript(javaScript);
    }

    File takeScreenshot() {
        return takesScreenshot.getScreenshotAs(OutputType.FILE);
    }

    DriverWrapper implicitWait(Duration duration) {
        driver.manage().timeouts().implicitlyWait(duration);
        return this;
    }

    DriverWrapper loadUrl(String url) {
        driver.get(url);
        return this;
    }

    int convertToInt(Object obj) {
        int result = -1;
        try {
            if (obj != null) {
                String resultString = String.valueOf(obj);
                result = Integer.parseInt(resultString);
            }
        } catch (Exception ex) {
        }
        return result;
    }

    @Override
    public void close() {
        driver.close();
    }

}
