package com.musicstore.order.api.v1.controllers;

import com.musicstore.order.api.v1.dto.*;
import com.musicstore.order.domain.entity.OrderStatus;
import com.musicstore.order.service.OrderService;
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

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Order Management", description = "Endpoints for order and checkout operations")
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Create a new order", description = "Places a new order with the provided items")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Order created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid order data")
    })
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest request) {
        OrderResponse order = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @Operation(summary = "Get order by ID", description = "Retrieves an order by its unique identifier")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order found"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(
            @Parameter(description = "Order ID") @PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrderById(orderId));
    }

    @Operation(summary = "Get order by order number", description = "Retrieves an order by its order number")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order found"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<OrderResponse> getOrderByNumber(
            @Parameter(description = "Order Number (e.g., ORD-20240101-ABC1234)") @PathVariable String orderNumber) {
        return ResponseEntity.ok(orderService.getOrderByNumber(orderNumber));
    }

    @Operation(summary = "Get all orders", description = "Retrieves a paginated list of all orders")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of orders returned")
    })
    @GetMapping
    public ResponseEntity<PagedResponse<OrderResponse>> getAllOrders(
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(orderService.getAllOrders(page, size));
    }

    @Operation(summary = "Get orders by user ID", description = "Retrieves orders for a specific user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orders found")
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByUserId(
            @Parameter(description = "User ID") @PathVariable Long userId) {
        return ResponseEntity.ok(orderService.getOrdersByUserId(userId));
    }

    @Operation(summary = "Get user orders paginated", description = "Retrieves paginated orders for a specific user")
    @GetMapping("/user/{userId}/page")
    public ResponseEntity<PagedResponse<OrderResponse>> getUserOrdersPaginated(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(orderService.getOrdersByUserId(userId, page, size));
    }

    @Operation(summary = "Get orders by status", description = "Retrieves orders filtered by status")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orders filtered by status")
    })
    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderResponse>> getOrdersByStatus(
            @Parameter(description = "Order status (e.g., PENDING, CONFIRMED, etc.)")
            @PathVariable OrderStatus status) {
        return ResponseEntity.ok(orderService.getOrdersByStatus(status));
    }

    @Operation(summary = "Update order status", description = "Updates the status of an existing order")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status updated"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @Parameter(description = "Order ID") @PathVariable Long orderId,
            @Valid @RequestBody OrderStatusUpdateRequest request) {
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, request));
    }

    @Operation(summary = "Cancel an order", description = "Cancels a pending or confirmed order")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order cancelled"),
            @ApiResponse(responseCode = "409", description = "Order cannot be cancelled"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @Parameter(description = "Order ID") @PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.cancelOrder(orderId));
    }

    @Operation(summary = "Checkout from cart", description = "Converts a cart into an order")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Checkout successful, order created"),
            @ApiResponse(responseCode = "400", description = "Invalid checkout data")
    })
    @PostMapping("/checkout")
    public ResponseEntity<OrderResponse> checkoutFromCart(@Valid @RequestBody CheckoutRequest request) {
        log.info("Checkout request for cart: {}, user: {}", request.cartId(), request.userId());
        // The actual checkout flow would fetch cart items and delegate to orderService
        // For now, this is a placeholder that demonstrates the checkout endpoint
        throw new UnsupportedOperationException("This endpoint requires cart integration. Use /orders with cart items.");
    }
}
