package com.tastygo;

import org.junit.Test;

import static org.junit.Assert.*;

import com.tastygo.model.Coupon;
import com.tastygo.model.Order;
import com.tastygo.util.DateUtil;
import com.tastygo.util.PasswordUtil;

public class Phase1UnitTest {

    @Test
    public void testPasswordHashingAndVerification() {
        String password = "Admin@123";
        String salt = PasswordUtil.generateSalt();
        assertNotNull(salt);
        assertEquals(32, salt.length()); // 16 bytes hex = 32 chars

        String hash = PasswordUtil.hashPassword(password, salt);
        assertNotNull(hash);
        assertEquals(64, hash.length()); // SHA-256 hex = 64 chars

        assertTrue(PasswordUtil.verifyPassword(password, salt, hash));
        assertFalse(PasswordUtil.verifyPassword("WrongPassword", salt, hash));
    }

    @Test
    public void testCouponDiscountCalculation() {
        // Percent discount: 20% on min order 15
        Coupon percentCoupon = new Coupon("TASTY20", "PERCENT", 20.0, 15.0, "2028-12-31", true);
        assertEquals(0.0, percentCoupon.calculateDiscount(10.0), 0.001); // Under min order
        assertEquals(4.0, percentCoupon.calculateDiscount(20.0), 0.001); // 20% of 20 = 4.0

        // Flat discount: $3.99 on min order 20
        Coupon flatCoupon = new Coupon("FREESHIP", "FLAT", 3.99, 20.0, "2028-12-31", true);
        assertEquals(0.0, flatCoupon.calculateDiscount(15.0), 0.001); // Under min order
        assertEquals(3.99, flatCoupon.calculateDiscount(25.0), 0.001);
    }

    @Test
    public void testOrderCanBeCancelledLogic() {
        Order order = new Order();
        order.setStatus(Order.STATUS_PLACED);
        assertTrue(order.canBeCancelled());

        order.setStatus(Order.STATUS_CONFIRMED);
        assertTrue(order.canBeCancelled());

        order.setStatus(Order.STATUS_PREPARING);
        assertFalse(order.canBeCancelled());

        order.setStatus(Order.STATUS_OUT_FOR_DELIVERY);
        assertFalse(order.canBeCancelled());

        order.setStatus(Order.STATUS_DELIVERED);
        assertFalse(order.canBeCancelled());
    }

    @Test
    public void testDateUtilNow() {
        String now = DateUtil.now();
        assertNotNull(now);
        assertTrue(now.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));
    }
}
