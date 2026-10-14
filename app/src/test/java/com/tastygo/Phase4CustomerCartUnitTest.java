package com.tastygo;

import org.junit.Test;

import static org.junit.Assert.*;

import com.tastygo.model.CartItem;
import com.tastygo.model.Coupon;
import com.tastygo.model.FoodItem;
import com.tastygo.model.FoodOption;
import com.tastygo.model.Order;

public class Phase4CustomerCartUnitTest {

    @Test
    public void testCartItemCalculations() {
        CartItem item = new CartItem(2L, 101L, 2, "Large", "Extra Cheese", "Extra sauce");
        item.setUnitPrice(12.50);

        assertEquals(2, item.getQuantity());
        assertEquals("Large", item.getSize());
        assertEquals("Extra Cheese", item.getAddons());
        assertEquals(25.00, item.getItemTotal(), 0.001);
    }

    @Test
    public void testCouponPercentageDiscount() {
        Coupon coupon = new Coupon("TASTY20", Coupon.TYPE_PERCENT, 20.0, 15.0, "2026-12-31", true);
        assertTrue(coupon.isPercentage());

        // Subtotal below min order
        assertEquals(0.0, coupon.calculateDiscount(10.0), 0.001);

        // Subtotal meets min order ($50 * 20% = $10)
        assertEquals(10.0, coupon.calculateDiscount(50.0), 0.001);
    }

    @Test
    public void testCouponFlatDiscount() {
        Coupon coupon = new Coupon("FLAT5", Coupon.TYPE_FLAT, 5.0, 20.0, "2026-12-31", true);
        assertFalse(coupon.isPercentage());

        // Subtotal meets min order
        assertEquals(5.0, coupon.calculateDiscount(25.0), 0.001);

        // Subtotal below min order
        assertEquals(0.0, coupon.calculateDiscount(18.0), 0.001);
    }

    @Test
    public void testOrderBillCalculation() {
        double subtotal = 24.00;
        double deliveryFee = subtotal >= 30.0 ? 0.0 : 2.99;
        double tax = Math.round(subtotal * 0.08 * 100.0) / 100.0;
        double discount = 4.80; // 20% discount
        double total = subtotal - discount + deliveryFee + tax;

        assertEquals(2.99, deliveryFee, 0.001);
        assertEquals(1.92, tax, 0.001);
        assertEquals(24.11, total, 0.001);

        // Free delivery over $30
        double bigSubtotal = 35.00;
        double freeDelivery = bigSubtotal >= 30.0 ? 0.0 : 2.99;
        assertEquals(0.0, freeDelivery, 0.001);
    }

    @Test
    public void testFoodItemFavoriteFlag() {
        FoodItem item = new FoodItem(1L, "Classic Truffle Burger", "Gourmet burger", 10.99, "", true, false, 4.9);
        assertFalse(item.isFavorite());

        item.setFavorite(true);
        assertTrue(item.isFavorite());
    }
}
