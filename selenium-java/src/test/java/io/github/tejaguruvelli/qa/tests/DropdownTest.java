package io.github.tejaguruvelli.qa.tests;

import io.github.tejaguruvelli.qa.pages.DropdownPage;
import io.github.tejaguruvelli.qa.support.BaseTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("smoke")
class DropdownTest extends BaseTest {

    @Test
    @DisplayName("Selecting an option by visible text updates the selected value")
    void selectOptionByVisibleText() {
        DropdownPage page = new DropdownPage(driver);
        page.open();
        assertEquals("Please select an option", page.selectedOptionText());

        page.selectByVisibleText("Option 2");

        assertEquals("Option 2", page.selectedOptionText());
    }
}
