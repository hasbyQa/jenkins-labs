package com.swaglabs.tests;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

/**
 * Parent class for all test classes.
 * Handles ChromeDriver setup and teardown once — no repetition.
 *
 * Each test gets a fresh browser (@BeforeEach) and it's
 * cleaned up after every test (@AfterEach) to ensure isolation.
 */
public abstract class BaseTest {

    // 'protected' so subclasses (LoginTest, CartTest, etc.) can use it directly
    protected WebDriver driver;

    @BeforeEach
    void setUp() {
        // WebDriverManager auto-downloads the right chromedriver binary
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();

        // Headless mode flags
        // Required for running inside Docker (no display server)
        options.addArguments("--headless=new");     // new headless mode (Chrome 112+)
        options.addArguments("--no-sandbox");        // required in Docker/root environments
        options.addArguments("--disable-dev-shm-usage"); // prevents shared memory issues in containers
        options.addArguments("--disable-gpu");       // not needed in headless mode
        options.addArguments("--window-size=1920,1080"); // consistent viewport for all tests

        driver = new ChromeDriver(options);
        driver.manage().deleteAllCookies(); // clean slate for every test
    }

    @AfterEach
    void tearDown() {
        // Always quit — releases browser process and avoids memory leaks
        if (driver != null) {
            driver.quit();
        }
    }
}