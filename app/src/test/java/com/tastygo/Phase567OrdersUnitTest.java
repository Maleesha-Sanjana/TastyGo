package com.tastygo;

import org.junit.Test;

import static org.junit.Assert.*;

import com.tastygo.model.Order;
import com.tastygo.model.OrderItem;
import com.tastygo.model.Review;

public class Phase567OrdersUnitTest {

    @Test
    public void testOrderLifecycleTransitions() {
        Order order = new Order();
        assertEquals(Order.STATUS_PLACED, order.getStatus());
        assertTrue(order.canBeCancelled());

        // Kitchen starts preparing
        order.setStatus(Order.STATUS_PREPARING);
        assertEquals(Order.STATUS_PREPARING, order.getStatus());
        assertFalse(order.canBeCancelled());

        // Dispatched for delivery
        order.setStatus(Order.STATUS_OUT_FOR_DELIVERY);
        assertEquals(Order.STATUS_OUT_FOR_DELIVERY, order.getStatus());
        assertFalse(order.canBeCancelled());

        // Customer receives order
        order.setStatus(Order.STATUS_DELIVERED);
        assertEquals(Order.STATUS_DELIVERED, order.getStatus());
        assertFalse(order.canBeCancelled());

        // Order cancelled
        Order cancelledOrder = new Order();
        cancelledOrder.setStatus(Order.STATUS_CANCELLED);
        assertEquals(Order.STATUS_CANCELLED, cancelledOrder.getStatus());
        assertFalse(cancelledOrder.canBeCancelled());
    }

    @Test
    public void testOrderItemSnapshots() {
        OrderItem item = new OrderItem(1L, 100L, 5L, "Classic Cheeseburger (Large) + Extra Bacon", 14.50, 2);

        assertEquals(1L, item.getId());
        assertEquals(100L, item.getOrderId());
        assertEquals(Long.valueOf(5L), item.getFoodId());
        assertEquals("Classic Cheeseburger (Large) + Extra Bacon", item.getNameSnapshot());
        assertEquals(14.50, item.getPriceSnapshot(), 0.001);
        assertEquals(2, item.getQuantity());

        double lineTotal = item.getPriceSnapshot() * item.getQuantity();
        assertEquals(29.00, lineTotal, 0.001);
    }

    @Test
    public void testReviewModelProperties() {
        Review review = new Review();
        review.setId(10L);
        review.setUserId(2L);
        review.setFoodId(5L);
        review.setRating(5.0);
        review.setComment("Delicious and freshly cooked!");
        review.setCreatedAt("2026-10-13 20:45:00");

        assertEquals(10L, review.getId());
        assertEquals(2L, review.getUserId());
        assertEquals(5L, review.getFoodId());
        assertEquals(5.0, review.getRating(), 0.001);
        assertEquals("Delicious and freshly cooked!", review.getComment());
        assertEquals("2026-10-13 20:45:00", review.getCreatedAt());
    }

    @Test
    public void testOrderBillIntegrity() {
        Order order = new Order();
        order.setSubtotal(36.00);
        order.setDiscount(5.00);
        order.setDeliveryFee(0.00); // free delivery over $30
        order.setTax(2.88);
        order.setTotal(order.getSubtotal() - order.getDiscount() + order.getDeliveryFee() + order.getTax());

        assertEquals(33.88, order.getTotal(), 0.001);
        assertEquals(Order.TYPE_DELIVERY, order.getOrderType());
    }
}
