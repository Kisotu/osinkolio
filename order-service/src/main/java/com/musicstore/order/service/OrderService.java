package com.musicstore.order.service;

import com.musicstore.order.api.v1.dto.*;
import com.musicstore.order.domain.entity.*;
import com.musicstore.order.domain.repository.OrderRepository;
import com.musicstore.order.kafka.OrderEventProducer;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderEventProducer orderEventProducer;

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        log.info("Creating order for user: {}", request.userId());

        Order order = Order.builder()
                .userId(request.userId())
                .orderNumber(generateOrderNumber())
                .status(OrderStatus.PENDING)
                .shippingAddressId(request.shippingAddressId())
                .billingAddressId(request.billingAddressId())
                .build();

        if (request.items() != null) {
            for (OrderItemRequest itemRequest : request.items()) {
                OrderItem item = OrderItem.builder()
                        .productId(itemRequest.productId())
                        .productName(itemRequest.productName())
                        .quantity(itemRequest.quantity())
                        .unitPrice(itemRequest.unitPrice())
                        .build();
                order.addItem(item);
            }
        }

        order.recalculateTotal();
        Order savedOrder = orderRepository.save(order);
        log.info("Order created with number: {}", savedOrder.getOrderNumber());

        orderEventProducer.publishOrderCreated(mapToOrderResponse(savedOrder));

        return mapToOrderResponse(savedOrder);
    }

    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + id));
        return mapToOrderResponse(order);
    }

    public OrderResponse getOrderByNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with number: " + orderNumber));
        return mapToOrderResponse(order);
    }

    public List<OrderResponse> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserIdWithItems(userId)
                .stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    public PagedResponse<OrderResponse> getOrdersByUserId(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Order> orderPage = orderRepository.findByUserId(userId, pageable);
        return mapToPagedResponse(orderPage);
    }

    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    public PagedResponse<OrderResponse> getAllOrders(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Order> orderPage = orderRepository.findAll(pageable);
        return mapToPagedResponse(orderPage);
    }

    public List<OrderResponse> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status)
                .stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long id, OrderStatusUpdateRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + id));

        log.info("Updating order {} status from {} to {}", id, order.getStatus(), request.status());
        order.setStatus(request.status());

        Order savedOrder = orderRepository.save(order);
        return mapToOrderResponse(savedOrder);
    }

    @Transactional
    public OrderResponse cancelOrder(Long id) {
        Order order = orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + id));

        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Only pending or confirmed orders can be cancelled");
        }

        log.info("Cancelling order: {}", id);
        order.setStatus(OrderStatus.CANCELLED);
        Order savedOrder = orderRepository.save(order);
        return mapToOrderResponse(savedOrder);
    }

    // Checkout flow: convert cart to order
    @Transactional
    public OrderResponse checkoutFromCart(Long userId, List<CartItemRequest> cartItems,
                                        Long shippingAddressId, Long billingAddressId) {
        log.info("Processing checkout for user: {}", userId);

        Order order = Order.builder()
                .userId(userId)
                .orderNumber(generateOrderNumber())
                .status(OrderStatus.PENDING)
                .shippingAddressId(shippingAddressId)
                .billingAddressId(billingAddressId)
                .build();

        for (CartItemRequest itemRequest : cartItems) {
            OrderItem item = OrderItem.builder()
                    .productId(itemRequest.productId())
                    .productName(itemRequest.productName() != null ? itemRequest.productName() : "Product " + itemRequest.productId())
                    .quantity(itemRequest.quantity())
                    .unitPrice(itemRequest.unitPrice())
                    .build();
            order.addItem(item);
        }

        order.recalculateTotal();
        Order savedOrder = orderRepository.save(order);
        log.info("Checkout complete. Order number: {}", savedOrder.getOrderNumber());

        return mapToOrderResponse(savedOrder);
    }

    private String generateOrderNumber() {
        return "ORD-" + OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private OrderResponse mapToOrderResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getId(),
                        item.getProductId(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getSubtotal()
                )).collect(Collectors.toList());

        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getUserId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getShippingAddressId(),
                order.getBillingAddressId(),
                itemResponses,
                order.getCreatedAt(),
                order.getUpdatedAt(),
                order.getVersion()
        );
    }

    private PagedResponse<OrderResponse> mapToPagedResponse(Page<Order> orderPage) {
        List<OrderResponse> content = orderPage.getContent()
                .stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                content,
                orderPage.getNumber(),
                orderPage.getSize(),
                orderPage.getTotalElements(),
                orderPage.getTotalPages(),
                orderPage.isFirst(),
                orderPage.isLast()
        );
    }
}
