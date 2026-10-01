package io.github.tejaguruvelli.qa.tests;

import io.github.tejaguruvelli.qa.pages.LoginPage;
import io.github.tejaguruvelli.qa.support.BaseTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("smoke")
class LoginTest extends BaseTest {

    // Published demo credentials for the-internet.herokuapp.com/login (not a secret).
    private static final String VALID_USER = "tomsmith";
    private static final String VALID_PASSWORD = "SuperSecretPassword!";

    @Test
    @DisplayName("Valid credentials land on the secure area with a success banner")
    void validLoginReachesSecureArea() {
        LoginPage login = new LoginPage(driver);
        login.open();
        login.loginAs(VALID_USER, VALID_PASSWORD);

        assertTrue(login.isOnSecureArea(), "expected /secure heading after login");
        assertEquals("You logged into a secure area!", login.flashMessage());
    }

    @Test
    @DisplayName("Wrong password stays on the login page and shows an error banner")
    void invalidLoginShowsError() {
        LoginPage login = new LoginPage(driver);
        login.open();
        login.loginAs(VALID_USER, "not-the-password");

        assertEquals("Your password is invalid!", login.flashMessage());
        assertFalse(login.currentUrl().contains("/secure"), "must not reach /secure");
    }
}
