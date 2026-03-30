package com.swaglabs.tests;

import com.swaglabs.pages.CartPage;
import com.swaglabs.pages.InventoryPage;
import com.swaglabs.pages.LoginPage;
import com.swaglabs.utils.TestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Cart feature.
 * Covers: adding items, cart badge, removing items, cart persistence.
 */
@DisplayName("Cart Tests")
class CartTest extends BaseTest {

    // Every cart test starts logged in on the inventory page
    private InventoryPage inventoryPage;

    @BeforeEach
    void loginAndGoToInventory() {
        inventoryPage = new LoginPage(driver)
                .open()
                .loginAs(TestConfig.VALID_USER, TestConfig.VALID_PASSWORD);
    }

    // Adding items

    @Test
    @DisplayName("Adding an item shows badge on cart icon")
    void addItem_cartBadgeShouldAppear() {
        inventoryPage.addFirstItemToCart();

        assertTrue(inventoryPage.isCartBadgeVisible(),
                "Cart badge should be visible after adding an item");
    }

    @Test
    @DisplayName("Cart badge shows correct count after adding one item")
    void addOneItem_badgeShouldShowOne() {
        inventoryPage.addFirstItemToCart();

        assertEquals("1", inventoryPage.getCartBadgeCount(),
                "Badge should show '1' after adding one item");
    }

    @Test
    @DisplayName("Adding two items shows badge count of 2")
    void addTwoItems_badgeShouldShowTwo() {
        inventoryPage
                .addItemToCart("sauce-labs-backpack")
                .addItemToCart("sauce-labs-bike-light");

        assertEquals("2", inventoryPage.getCartBadgeCount(),
                "Badge should show '2' after adding two items");
    }

    // Cart page

    @Test
    @DisplayName("Cart page loads with the added item present")
    void cartPage_shouldContainAddedItem() {
        inventoryPage.addItemToCart("sauce-labs-backpack");
        CartPage cartPage = inventoryPage.goToCart();

        assertTrue(cartPage.isLoaded(), "Cart page should load successfully");
        assertTrue(cartPage.hasItems(), "Cart should contain the added item");
    }

    @Test
    @DisplayName("Item name in cart matches what was added")
    void cartItem_nameShouldMatchAddedProduct() {
        inventoryPage.addItemToCart("sauce-labs-backpack");
        CartPage cartPage = inventoryPage.goToCart();

        assertTrue(cartPage.getFirstItemName().toLowerCase().contains("backpack"),
                "Cart item name should contain 'backpack'");
    }

    //  Removing items

    @Test
    @DisplayName("Removing the only item empties the cart")
    void removeItem_cartShouldBeEmpty() {
        inventoryPage.addFirstItemToCart();
        CartPage cartPage = inventoryPage.goToCart();

        cartPage.removeFirstItem();

        assertFalse(cartPage.hasItems(),
                "Cart should be empty after removing the only item");
    }

    @Test
    @DisplayName("Cart badge disappears when cart is emptied")
    void removeAllItems_badgeShouldDisappear() {
        inventoryPage.addFirstItemToCart();
        CartPage cartPage = inventoryPage.goToCart();
        cartPage.removeFirstItem();

        // Go back and check badge is gone
        cartPage.continueShopping();

        assertFalse(inventoryPage.isCartBadgeVisible(),
                "Cart badge should not be visible when cart is empty");
    }
}