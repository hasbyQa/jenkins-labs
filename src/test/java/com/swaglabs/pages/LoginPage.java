package com.swaglabs.pages;

import com.swaglabs.utils.TestConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

// Represents the Swag Labs login page (saucedemo.com)
public class LoginPage extends BasePage {

    // Using id attributes here — they're stable and unique on this page
    private static final By USERNAME_INPUT = By.id("user-name");
    private static final By PASSWORD_INPUT = By.id("password");
    private static final By LOGIN_BUTTON   = By.id("login-button");
    private static final By ERROR_MESSAGE  = By.cssSelector("[data-test='error']");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    // Opens the login page
    public LoginPage open() {
        driver.get(TestConfig.BASE_URL);
        return this;
    }

    // Logs in with given credentials and waits for inventory page to be in the URL
    public InventoryPage loginAs(String username, String password) {
        type(USERNAME_INPUT, username);
        type(PASSWORD_INPUT, password);
        click(LOGIN_BUTTON);
        waitForUrlToContain("inventory");
        return new InventoryPage(driver);
    }

    // Attempts login without navigating away — used for failure test scenarios
    public void attemptLogin(String username, String password) {
        type(USERNAME_INPUT, username);
        type(PASSWORD_INPUT, password);
        click(LOGIN_BUTTON);
    }

    public String getErrorMessage() {
        return getText(ERROR_MESSAGE);
    }

    public boolean isErrorDisplayed() {
        return isVisible(ERROR_MESSAGE);
    }
}