package io.github.tejaguruvelli.qa.support;

import io.github.tejaguruvelli.qa.driver.DriverFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.WebDriver;

/** One fresh browser per test method: no shared state, no ordering dependencies. */
@ExtendWith(ScreenshotOnFailure.class)
public abstract class BaseTest implements ScreenshotOnFailure.DriverHolder {

    protected WebDriver driver;

    @BeforeEach
    void startBrowser() {
        driver = DriverFactory.create();
    }

    @AfterEach
    void stopBrowser() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Override
    public WebDriver driver() {
        return driver;
    }
}
