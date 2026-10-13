package com.tastygo;

import org.junit.Test;

import static org.junit.Assert.*;

import com.tastygo.model.Category;
import com.tastygo.model.FoodItem;
import com.tastygo.model.FoodOption;

import java.util.ArrayList;
import java.util.List;

public class Phase3AdminMenuUnitTest {

    @Test
    public void testCategoryModel() {
        Category category = new Category(1, "Burgers", "ic_cat_burger");
        assertEquals(1, category.getId());
        assertEquals("Burgers", category.getName());
        assertEquals("ic_cat_burger", category.getImagePath());
    }

    @Test
    public void testFoodItemOptionsRelationship() {
        FoodItem item = new FoodItem(1, 1, "Classic Cheeseburger", "Delicious beef burger", 8.99, "food_burger_classic", true, false, 4.8);

        List<FoodOption> options = new ArrayList<>();
        options.add(new FoodOption(1, 1, "SIZE", "Regular", 0.0));
        options.add(new FoodOption(2, 1, "SIZE", "Double", 2.50));
        options.add(new FoodOption(3, 1, "ADDON", "Extra Cheese", 0.99));

        item.setOptions(options);

        assertEquals(3, item.getOptions().size());
        assertTrue(item.getOptions().get(0).isSize());
        assertFalse(item.getOptions().get(0).isAddon());
        assertTrue(item.getOptions().get(2).isAddon());
        assertEquals(2.50, item.getOptions().get(1).getExtraPrice(), 0.001);
    }

    @Test
    public void testFoodItemAvailabilityAndDietary() {
        FoodItem vegItem = new FoodItem(2, "Veggie Delight", "Healthy bowl", 7.50, "", true, true, 4.5);
        assertTrue(vegItem.isVeg());
        assertTrue(vegItem.isAvailable());

        vegItem.setAvailable(false);
        assertFalse(vegItem.isAvailable());

        FoodItem nonVegItem = new FoodItem(3, "BBQ Ribs", "Savory meat", 12.99, "", true, false, 4.9);
        assertFalse(nonVegItem.isVeg());
    }
}
