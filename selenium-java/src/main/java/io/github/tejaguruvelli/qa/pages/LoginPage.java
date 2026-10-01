package io.github.tejaguruvelli.qa.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/** /login on the-internet: username + password form with a flash banner. */
public class LoginPage extends BasePage {

    private static final By USERNAME = By.id("username");
    private static final By PASSWORD = By.id("password");
    private static final By SUBMIT = By.cssSelector("button[type='submit']");
    private static final By FLASH = By.id("flash");
    private static final By SECURE_HEADING = By.cssSelector("div.example h2");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    @Override
    protected String path() {
        return "/login";
    }

    public LoginPage loginAs(String username, String password) {
        visible(USERNAME).sendKeys(username);
        visible(PASSWORD).sendKeys(password);
        clickable(SUBMIT).click();
        return this;
    }

    /** Text of the flash banner once it is rendered, with the trailing close glyph removed. */
    public String flashMessage() {
        return visible(FLASH).getText().replace("×", "").trim();
    }

    public boolean isOnSecureArea() {
        waitForUrlContains("/secure");
        return visible(SECURE_HEADING).getText().contains("Secure Area");
    }
}
