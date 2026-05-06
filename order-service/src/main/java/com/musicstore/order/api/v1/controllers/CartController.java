package com.musicstore.order.api.v1.controllers;

import com.musicstore.order.api.v1.dto.CartItemRequest;
import com.musicstore.order.api.v1.dto.CartResponse;
import com.musicstore.order.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/carts")
@RequiredArgsConstructor
@Tag(name = "Cart Management", description = "Endpoints for shopping cart operations")
public class CartController {

    private final CartService cartService;

    @Operation(summary = "Create a new cart", description = "Creates a new shopping cart for a user or session")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cart created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping
    public ResponseEntity<CartResponse> createCart(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String sessionId) {
        CartResponse cart = cartService.createCart(userId, sessionId);
        return ResponseEntity.status(HttpStatus.CREATED).body(cart);
    }

    @Operation(summary = "Get cart by ID", description = "Retrieves a cart by its unique identifier")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cart found"),
            @ApiResponse(responseCode = "404", description = "Cart not found")
    })
    @GetMapping("/{cartId}")
    public ResponseEntity<CartResponse> getCartById(
            @Parameter(description = "Cart ID") @PathVariable Long cartId) {
        return ResponseEntity.ok(cartService.getCartById(cartId));
    }

    @Operation(summary = "Get cart by user ID", description = "Retrieves the active cart for a specific user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cart found or empty cart returned")
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<CartResponse> getCartByUserId(
            @Parameter(description = "User ID") @PathVariable Long userId) {
        return ResponseEntity.ok(cartService.getCartByUserId(userId));
    }

    @Operation(summary = "Get cart by session ID", description = "Retrieves the active cart for a specific session")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cart found or empty cart returned")
    })
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<CartResponse> getCartBySessionId(
            @Parameter(description = "Session ID") @PathVariable String sessionId) {
        return ResponseEntity.ok(cartService.getCartBySessionId(sessionId));
    }

    @Operation(summary = "Add item to cart", description = "Adds a product to the specified cart")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item added successfully"),
            @ApiResponse(responseCode = "404", description = "Cart not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping("/{cartId}/items")
    public ResponseEntity<CartResponse> addItemToCart(
            @Parameter(description = "Cart ID") @PathVariable Long cartId,
            @Valid @RequestBody CartItemRequest itemRequest) {
        return ResponseEntity.ok(cartService.addItemToCart(cartId, itemRequest));
    }

    @Operation(summary = "Update item quantity", description = "Updates the quantity of a specific product in the cart")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Quantity updated"),
            @ApiResponse(responseCode = "404", description = "Cart or item not found"),
            @ApiResponse(responseCode = "400", description = "Invalid quantity")
    })
    @PutMapping("/{cartId}/items/{productId}")
    public ResponseEntity<CartResponse> updateItemQuantity(
            @Parameter(description = "Cart ID") @PathVariable Long cartId,
            @Parameter(description = "Product ID") @PathVariable Long productId,
            @RequestBody Map<String, Integer> request) {
        Integer quantity = request.getOrDefault("quantity", 1);
        return ResponseEntity.ok(cartService.updateItemQuantity(cartId, productId, quantity));
    }

    @Operation(summary = "Remove item from cart", description = "Removes a specific product from the cart")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item removed"),
            @ApiResponse(responseCode = "404", description = "Cart or item not found")
    })
    @DeleteMapping("/{cartId}/items/{productId}")
    public ResponseEntity<CartResponse> removeItemFromCart(
            @Parameter(description = "Cart ID") @PathVariable Long cartId,
            @Parameter(description = "Product ID") @PathVariable Long productId) {
        return ResponseEntity.ok(cartService.removeItemFromCart(cartId, productId));
    }

    @Operation(summary = "Clear cart", description = "Removes all items from the cart")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cart cleared"),
            @ApiResponse(responseCode = "404", description = "Cart not found")
    })
    @DeleteMapping("/{cartId}/items")
    public ResponseEntity<Void> clearCart(@Parameter(description = "Cart ID") @PathVariable Long cartId) {
        cartService.clearCart(cartId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete cart", description = "Permanently deletes a cart")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cart deleted"),
            @ApiResponse(responseCode = "404", description = "Cart not found")
    })
    @DeleteMapping("/{cartId}")
    public ResponseEntity<Void> deleteCart(@Parameter(description = "Cart ID") @PathVariable Long cartId) {
        cartService.deleteCart(cartId);
        return ResponseEntity.noContent().build();
    }
}
