package io.github.tejaguruvelli.qa.driver;

import io.github.tejaguruvelli.qa.config.TestConfig;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.time.Duration;
import java.util.List;

/**
 * Builds a WebDriver from BROWSER and HEADLESS environment variables.
 * Default: Chrome, headless. Selenium Manager (bundled with Selenium 4)
 * downloads the matching driver binary, so there is no WebDriverManager and no
 * driver on the PATH.
 *
 * Implicit waits are deliberately left at zero: every synchronisation in the
 * suite is an explicit wait owned by the page object that needs it.
 */
public final class DriverFactory {

    private static final List<String> CHROMIUM_FLAGS = List.of(
        "--window-size=1366,900",
        "--disable-gpu",
        "--no-sandbox",
        "--disable-dev-shm-usage"
    );

    private DriverFactory() {
    }

    public static WebDriver create() {
        return create(TestConfig.browser(), TestConfig.headless());
    }

    public static WebDriver create(String browser, boolean headless) {
        WebDriver driver = switch (browser) {
            case "chrome" -> new ChromeDriver(chromeOptions(headless));
            case "edge" -> new EdgeDriver(edgeOptions(headless));
            case "firefox" -> new FirefoxDriver(firefoxOptions(headless));
            default -> throw new IllegalArgumentException(
                "Unsupported BROWSER='" + browser + "'. Use chrome, edge or firefox.");
        };
        driver.manage().timeouts().implicitlyWait(Duration.ZERO);
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));
        return driver;
    }

    private static ChromeOptions chromeOptions(boolean headless) {
        ChromeOptions options = new ChromeOptions();
        options.addArguments(CHROMIUM_FLAGS);
        if (headless) {
            options.addArguments("--headless=new");
        }
        return options;
    }

    private static EdgeOptions edgeOptions(boolean headless) {
        EdgeOptions options = new EdgeOptions();
        options.addArguments(CHROMIUM_FLAGS);
        if (headless) {
            options.addArguments("--headless=new");
        }
        return options;
    }

    private static FirefoxOptions firefoxOptions(boolean headless) {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--width=1366", "--height=900");
        if (headless) {
            options.addArguments("-headless");
        }
        return options;
    }
}
