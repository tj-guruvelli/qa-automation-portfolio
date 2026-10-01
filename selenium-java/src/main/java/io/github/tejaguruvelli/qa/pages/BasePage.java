package io.github.tejaguruvelli.qa.pages;

import io.github.tejaguruvelli.qa.config.TestConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Common behaviour for every page object: navigation relative to the base URL
 * and explicit-wait helpers. Subclasses never call Thread.sleep; they describe
 * the condition they need and let WebDriverWait poll for it.
 */
public abstract class BasePage {

    protected final WebDriver driver;
    protected final WebDriverWait wait;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, TestConfig.explicitWait());
    }

    /** Path of this page relative to the base URL, e.g. "/login". */
    protected abstract String path();

    public void open() {
        driver.get(TestConfig.baseUrl() + path());
    }

    public String currentUrl() {
        return driver.getCurrentUrl();
    }

    protected WebElement visible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected WebElement clickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    protected void waitForInvisibility(By locator) {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    protected void waitForUrlContains(String fragment) {
        wait.until(ExpectedConditions.urlContains(fragment));
    }
}
