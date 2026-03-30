package com.swaglabs.pages;

import com.swaglabs.utils.TestConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

// Parent class for all page objects — wraps raw Selenium calls into clean reusable methods
public abstract class BasePage {

    protected final WebDriver driver;
    protected final WebDriverWait wait;

    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(TestConfig.TIMEOUT_SECONDS));
    }

    // Standard click — waits for element to be clickable
    protected void click(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
    }

    // JavaScript click — bypasses Chrome headless rendering quirks
    // Required for <input type="submit"> and deeply nested elements in Docker
    protected void jsClick(By locator) {
        WebElement el = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
    }

    // Waits for the element, clears it, then types the text
    protected void type(By locator, String text) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        el.clear();
        el.sendKeys(text);
    }

    // Returns the visible text of an element, trimmed
    protected String getText(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).getText().trim();
    }

    // Returns true if the element is visible on the page, false otherwise
    protected boolean isVisible(By locator) {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    // Waits for element to be visible (has dimensions and is painted)
    // Use for elements you will interact with or assert text on
    protected void waitForElementVisible(By locator) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    // Waits for element to exist in the DOM — does NOT require it to be painted
    // More reliable in Docker/headless where React renders but painting lags
    protected void waitForElementPresent(By locator) {
        wait.until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    // Blocks until the browser URL contains the given string
    protected void waitForUrlToContain(String fragment) {
        wait.until(ExpectedConditions.urlContains(fragment));
    }

    protected String getCurrentUrl() {
        return driver.getCurrentUrl();
    }
}