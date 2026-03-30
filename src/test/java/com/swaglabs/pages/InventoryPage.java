package com.swaglabs.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

// Represents the Products page — the landing page after login (/inventory.html)
public class InventoryPage extends BasePage {

    private static final By PAGE_TITLE        = By.cssSelector("[data-test='title']");
    private static final By CART_ICON         = By.cssSelector("[data-test='shopping-cart-link']");
    private static final By CART_BADGE        = By.cssSelector("[data-test='shopping-cart-badge']");
    private static final By FIRST_ADD_TO_CART = By.cssSelector("[data-test^='add-to-cart']");

    public InventoryPage(WebDriver driver) {
        super(driver);
    }

    // True when the Products title is visible — confirms we're on this page
    public boolean isLoaded() {
        return isVisible(PAGE_TITLE);
    }

    public String getPageTitle() {
        return getText(PAGE_TITLE);
    }

    // Adds the first product listed on the page
    public InventoryPage addFirstItemToCart() {
        click(FIRST_ADD_TO_CART);
        return this;
    }

    // Adds a specific product using its slug e.g. "sauce-labs-backpack"
    public InventoryPage addItemToCart(String productSlug) {
        click(By.cssSelector("[data-test='add-to-cart-" + productSlug + "']"));
        return this;
    }

    // Returns the badge number shown on the cart icon e.g. "1", "2"
    public String getCartBadgeCount() {
        return getText(CART_BADGE);
    }

    // True if the cart badge is showing (means at least 1 item in cart)
    public boolean isCartBadgeVisible() {
        return isVisible(CART_BADGE);
    }

    // Clicks the cart icon and waits for the cart URL before returning
    public CartPage goToCart() {
        click(CART_ICON);
        waitForUrlToContain("cart");
        return new CartPage(driver);
    }
}