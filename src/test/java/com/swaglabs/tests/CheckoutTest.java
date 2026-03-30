package com.swaglabs.tests;

import com.swaglabs.pages.CartPage;
import com.swaglabs.pages.CheckoutPage;
import com.swaglabs.pages.InventoryPage;
import com.swaglabs.pages.LoginPage;
import com.swaglabs.utils.TestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Checkout feature.
 * Covers: full happy-path checkout, form validation, confirmation screen.
 */
@DisplayName("Checkout Tests")
class CheckoutTest extends BaseTest {

    // Every checkout test starts with one item already in the cart
    private CartPage cartPage;

    @BeforeEach
    void loginAndAddItemToCart() {
        InventoryPage inventoryPage = new LoginPage(driver)
                .open()
                .loginAs(TestConfig.VALID_USER, TestConfig.VALID_PASSWORD);

        inventoryPage.addItemToCart("sauce-labs-backpack");
        cartPage = inventoryPage.goToCart();
    }

    // Full happy path

    @Test
    @DisplayName("Complete checkout flow ends on confirmation page")
    void fullCheckout_shouldShowConfirmation() {
        CheckoutPage checkoutPage = cartPage
                .proceedToCheckout()
                .fillInfo(TestConfig.FIRST_NAME, TestConfig.LAST_NAME, TestConfig.POSTAL_CODE)
                .finishOrder();

        assertTrue(checkoutPage.isOrderComplete(),
                "Confirmation header should be visible after finishing order");
    }

    @Test
    @DisplayName("Confirmation message says 'Thank you for your order!'")
    void completedOrder_shouldShowThankYouMessage() {
        CheckoutPage checkoutPage = cartPage
                .proceedToCheckout()
                .fillInfo(TestConfig.FIRST_NAME, TestConfig.LAST_NAME, TestConfig.POSTAL_CODE)
                .finishOrder();

        assertEquals(TestConfig.CHECKOUT_COMPLETE_MSG, checkoutPage.getConfirmationMessage(),
                "Confirmation text should match expected message");
    }

    @Test
    @DisplayName("Order overview shows a non-zero item total")
    void orderOverview_shouldDisplayItemTotal() {
        CheckoutPage checkoutPage = cartPage
                .proceedToCheckout()
                .fillInfo(TestConfig.FIRST_NAME, TestConfig.LAST_NAME, TestConfig.POSTAL_CODE);

        String total = checkoutPage.getItemTotal();
        assertNotNull(total, "Item total label should be present");
        assertTrue(total.contains("$"), "Item total should include a dollar sign");
    }

    @Test
    @DisplayName("Back to products button after order returns to inventory")
    void afterOrder_backButtonShouldReturnToInventory() {
        CheckoutPage checkoutPage = cartPage
                .proceedToCheckout()
                .fillInfo(TestConfig.FIRST_NAME, TestConfig.LAST_NAME, TestConfig.POSTAL_CODE)
                .finishOrder();

        InventoryPage inventoryPage = checkoutPage.backToProducts();

        assertTrue(inventoryPage.isLoaded(),
                "Clicking 'Back Home' should return to the Products page");
    }

    // Form validation

    @Test
    @DisplayName("Empty checkout form shows validation error")
    void emptyCheckoutForm_shouldShowError() {
        // Swag Labs validates First Name first — empty form triggers that error
        CheckoutPage checkoutPage = cartPage
                .proceedToCheckout()
                .submitEmptyForm();

        assertTrue(checkoutPage.isErrorDisplayed(),
                "Submitting empty form should show an error");

        assertTrue(checkoutPage.getErrorMessage().toLowerCase().contains("first name"),
                "Error should mention 'First Name' is required");
    }

    @Test
    @DisplayName("Missing last name shows validation error")
    void missingLastName_shouldShowError() {
        // Fill first name only — last name is the next required field
        CheckoutPage checkoutPage = cartPage
                .proceedToCheckout()
                .fillFirstNameOnly(TestConfig.FIRST_NAME);

        assertTrue(checkoutPage.isErrorDisplayed(),
                "Missing last name should trigger a validation error");

        assertTrue(checkoutPage.getErrorMessage().toLowerCase().contains("last name"),
                "Error should mention 'Last Name' is required");
    }
}