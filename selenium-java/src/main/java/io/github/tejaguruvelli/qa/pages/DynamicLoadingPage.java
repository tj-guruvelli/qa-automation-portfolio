package io.github.tejaguruvelli.qa.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * /dynamic_loading/2 on the-internet: clicking Start shows a spinner for a few
 * seconds, then renders a "Hello World!" element that did not exist before.
 */
public class DynamicLoadingPage extends BasePage {

    private static final By START_BUTTON = By.cssSelector("#start button");
    private static final By LOADING = By.id("loading");
    private static final By FINISH_TEXT = By.cssSelector("#finish h4");

    public DynamicLoadingPage(WebDriver driver) {
        super(driver);
    }

    @Override
    protected String path() {
        return "/dynamic_loading/2";
    }

    public DynamicLoadingPage start() {
        clickable(START_BUTTON).click();
        return this;
    }

    /** Blocks until the spinner is gone and the result is visible, then returns the result text. */
    public String waitForResult() {
        waitForInvisibility(LOADING);
        return visible(FINISH_TEXT).getText().trim();
    }
}
