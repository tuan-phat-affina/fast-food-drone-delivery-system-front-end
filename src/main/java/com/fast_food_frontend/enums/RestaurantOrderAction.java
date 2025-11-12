package com.fast_food_frontend.enums;

public enum RestaurantOrderAction {
    ACCEPT,   // PENDING -> PREPARING
    REJECT,   // PENDING -> CANCELLED (with reason)
    CANCEL    // PENDING -> CANCELLED (restaurant initiated cancel)
}
