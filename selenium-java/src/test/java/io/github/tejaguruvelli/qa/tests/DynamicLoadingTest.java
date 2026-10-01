package io.github.tejaguruvelli.qa.tests;

import io.github.tejaguruvelli.qa.pages.DynamicLoadingPage;
import io.github.tejaguruvelli.qa.support.BaseTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("smoke")
class DynamicLoadingTest extends BaseTest {

    @Test
    @DisplayName("Result element rendered after an async delay is waited for explicitly")
    void waitsForDynamicallyRenderedElement() {
        DynamicLoadingPage page = new DynamicLoadingPage(driver);
        page.open();

        String result = page.start().waitForResult();

        assertEquals("Hello World!", result);
    }
}
