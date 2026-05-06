package com.musicstore.order.api.v1.dto;

import jakarta.validation.constraints.NotNull;

public record CheckoutRequest(
        @NotNull(message = "Cart ID is required")
        Long cartId,

        @NotNull(message = "User ID is required")
        Long userId,

        Long shippingAddressId,
        Long billingAddressId
) {}
