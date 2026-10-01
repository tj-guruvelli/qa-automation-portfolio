package io.github.tejaguruvelli.qa.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Select;

/** /dropdown on the-internet: a single native select element. */
public class DropdownPage extends BasePage {

    private static final By DROPDOWN = By.id("dropdown");

    public DropdownPage(WebDriver driver) {
        super(driver);
    }

    @Override
    protected String path() {
        return "/dropdown";
    }

    public DropdownPage selectByVisibleText(String text) {
        new Select(clickable(DROPDOWN)).selectByVisibleText(text);
        return this;
    }

    public String selectedOptionText() {
        return new Select(visible(DROPDOWN)).getFirstSelectedOption().getText().trim();
    }
}
