package io.github.tejaguruvelli.qa.tests;

import io.github.tejaguruvelli.qa.pages.StatusCodePage;
import io.github.tejaguruvelli.qa.support.BaseTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Negative path: the server answers 404 and the page says so. */
@Tag("negative")
class StatusCodeTest extends BaseTest {

    @Test
    @DisplayName("404 status page explains the status code")
    void notFoundPageExplainsStatus() {
        StatusCodePage page = new StatusCodePage(driver, 404);
        page.open();

        String text = page.explanationText();

        assertTrue(text.contains("This page returned a 404 status code."),
            "unexpected explanation text: " + text);
    }
}
