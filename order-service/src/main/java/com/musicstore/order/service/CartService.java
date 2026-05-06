package com.musicstore.order.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.musicstore.order.api.v1.dto.CartItemRequest;
import com.musicstore.order.api.v1.dto.CartItemResponse;
import com.musicstore.order.api.v1.dto.CartResponse;
import com.musicstore.order.domain.entity.Cart;
import com.musicstore.order.domain.entity.CartItem;
import com.musicstore.order.domain.repository.CartRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

    private final CartRepository cartRepository;
    private final ObjectMapper objectMapper;

    public CartResponse getCartById(Long id) {
        Cart cart = cartRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cart not found with id: " + id));
        return mapToCartResponse(cart);
    }

    public CartResponse getCartByUserId(Long userId) {
        Cart cart = cartRepository.findActiveCartByUserId(userId, OffsetDateTime.now())
                .orElse(null);
        if (cart == null) {
            // Return empty cart
            return new CartResponse(null, userId, null, List.of(), BigDecimal.ZERO, 0, null);
        }
        deserializeItems(cart);
        return mapToCartResponse(cart);
    }

    public CartResponse getCartBySessionId(String sessionId) {
        Cart cart = cartRepository.findActiveCartBySessionId(sessionId, OffsetDateTime.now())
                .orElse(null);
        if (cart == null) {
            return new CartResponse(null, null, sessionId, List.of(), BigDecimal.ZERO, 0, null);
        }
        deserializeItems(cart);
        return mapToCartResponse(cart);
    }

    @Transactional
    public CartResponse createCart(Long userId, String sessionId) {
        if (userId != null) {
            // Check if user already has an active cart
            Optional<Cart> existingCart = cartRepository.findActiveCartByUserId(userId, OffsetDateTime.now());
            if (existingCart.isPresent()) {
                return getCartByUserId(userId);
            }
        }

        Cart cart = Cart.builder()
                .userId(userId)
                .sessionId(sessionId)
                .expiresAt(OffsetDateTime.now().plusHours(24))
                .build();
        cart.setItems(new ArrayList<>());

        Cart savedCart = cartRepository.save(cart);
        return mapToCartResponse(savedCart);
    }

    @Transactional
    public CartResponse addItemToCart(Long cartId, CartItemRequest itemRequest) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new EntityNotFoundException("Cart not found with id: " + cartId));

        deserializeItems(cart);

        // Check if item already exists in cart
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(itemRequest.productId()))
                .findFirst();

        if (existingItem.isPresent()) {
            existingItem.get().setQuantity(existingItem.get().getQuantity() + itemRequest.quantity());
        } else {
            CartItem newItem = CartItem.builder()
                    .productId(itemRequest.productId())
                    .productName(itemRequest.productName())
                    .quantity(itemRequest.quantity())
                    .unitPrice(itemRequest.unitPrice())
                    .imageUrl(itemRequest.imageUrl())
                    .build();
            cart.getItems().add(newItem);
        }

        serializeItems(cart);
        cart.setUpdatedAt(OffsetDateTime.now());
        Cart savedCart = cartRepository.save(cart);

        return mapToCartResponse(savedCart);
    }

    @Transactional
    public CartResponse updateItemQuantity(Long cartId, Long productId, Integer quantity) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new EntityNotFoundException("Cart not found with id: " + cartId));

        deserializeItems(cart);

        Optional<CartItem> itemOpt = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst();

        if (itemOpt.isEmpty()) {
            throw new EntityNotFoundException("Item not found in cart with productId: " + productId);
        }

        if (quantity <= 0) {
            cart.getItems().remove(itemOpt.get());
        } else {
            itemOpt.get().setQuantity(quantity);
        }

        serializeItems(cart);
        cart.setUpdatedAt(OffsetDateTime.now());
        Cart savedCart = cartRepository.save(cart);

        return mapToCartResponse(savedCart);
    }

    @Transactional
    public CartResponse removeItemFromCart(Long cartId, Long productId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new EntityNotFoundException("Cart not found with id: " + cartId));

        deserializeItems(cart);

        boolean removed = cart.getItems().removeIf(item -> item.getProductId().equals(productId));
        if (!removed) {
            throw new EntityNotFoundException("Item not found in cart with productId: " + productId);
        }

        serializeItems(cart);
        cart.setUpdatedAt(OffsetDateTime.now());
        Cart savedCart = cartRepository.save(cart);

        return mapToCartResponse(savedCart);
    }

    @Transactional
    public void clearCart(Long cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new EntityNotFoundException("Cart not found with id: " + cartId));

        cart.setItems(new ArrayList<>());
        cart.setUpdatedAt(OffsetDateTime.now());
        serializeItems(cart);
        cartRepository.save(cart);
        log.info("Cleared cart: {}", cartId);
    }

    @Transactional
    public void deleteCart(Long cartId) {
        if (!cartRepository.existsById(cartId)) {
            throw new EntityNotFoundException("Cart not found with id: " + cartId);
        }
        cartRepository.deleteById(cartId);
        log.info("Deleted cart: {}", cartId);
    }

    @Transactional
    public int clearExpiredCarts() {
        int count = cartRepository.deleteExpiredCarts(OffsetDateTime.now());
        log.info("Cleared {} expired carts", count);
        return count;
    }

    private void serializeItems(Cart cart) {
        try {
            if (cart.getItems() != null && !cart.getItems().isEmpty()) {
                cart.setItemsJson(objectMapper.writeValueAsString(cart.getItems()));
            } else {
                cart.setItemsJson("[]");
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize cart items", e);
            throw new RuntimeException("Failed to serialize cart items", e);
        }
    }

    private void deserializeItems(Cart cart) {
        try {
            if (cart.getItemsJson() != null && !cart.getItemsJson().isEmpty()) {
                cart.setItems(objectMapper.readValue(cart.getItemsJson(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, CartItem.class)));
            } else {
                cart.setItems(new ArrayList<>());
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize cart items", e);
            cart.setItems(new ArrayList<>());
        }
    }

    private CartResponse mapToCartResponse(Cart cart) {
        List<CartItem> items = cart.getItems() != null ? cart.getItems() : new ArrayList<>();

        List<CartItemResponse> itemResponses = items.stream()
                .map(item -> new CartItemResponse(
                        item.getProductId(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getSubtotal(),
                        item.getImageUrl()
                )).collect(Collectors.toList());

        BigDecimal total = items.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalItems = items.stream().mapToInt(CartItem::getQuantity).sum();

        return new CartResponse(
                cart.getId(),
                cart.getUserId(),
                cart.getSessionId(),
                itemResponses,
                total,
                totalItems,
                cart.getExpiresAt()
        );
    }
}
