package io.github.tejaguruvelli.qa.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/** /status_codes/{code} on the-internet: the server responds with that HTTP status and a short explanation. */
public class StatusCodePage extends BasePage {

    private static final By EXAMPLE_TEXT = By.cssSelector("div.example p");

    private final int statusCode;

    public StatusCodePage(WebDriver driver, int statusCode) {
        super(driver);
        this.statusCode = statusCode;
    }

    @Override
    protected String path() {
        return "/status_codes/" + statusCode;
    }

    public String explanationText() {
        return visible(EXAMPLE_TEXT).getText().trim();
    }
}
