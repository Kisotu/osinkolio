package com.musicstore.user.service;

import com.musicstore.user.api.v1.dto.AddressRequest;
import com.musicstore.user.api.v1.dto.AddressResponse;
import com.musicstore.user.domain.entity.Address;
import com.musicstore.user.domain.entity.User;
import com.musicstore.user.domain.repository.AddressRepository;
import com.musicstore.user.domain.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public List<AddressResponse> getAddressesByUserId(Long userId) {
        return addressRepository.findByUserId(userId).stream()
                .map(this::mapToAddressResponse)
                .collect(Collectors.toList());
    }

    public AddressResponse getAddressById(Long addressId, Long userId) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Address not found with id: " + addressId));
        return mapToAddressResponse(address);
    }

    @Transactional
    public AddressResponse createAddress(Long userId, AddressRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        // If this is the first address or marked as default, make it default
        boolean isDefault = Boolean.TRUE.equals(request.isDefault())
                || addressRepository.countByUserId(userId) == 0;

        if (isDefault) {
            clearDefaultAddress(userId);
        }

        Address address = Address.builder()
                .user(user)
                .label(request.label())
                .street(request.street())
                .city(request.city())
                .state(request.state())
                .postalCode(request.postalCode())
                .country(request.country())
                .isDefault(isDefault)
                .build();

        Address savedAddress = addressRepository.save(address);
        log.info("Address created for user: {} (address ID: {})", userId, savedAddress.getId());
        return mapToAddressResponse(savedAddress);
    }

    @Transactional
    public AddressResponse updateAddress(Long addressId, Long userId, AddressRequest request) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Address not found with id: " + addressId));

        if (request.street() != null) address.setStreet(request.street());
        if (request.city() != null) address.setCity(request.city());
        if (request.state() != null) address.setState(request.state());
        if (request.postalCode() != null) address.setPostalCode(request.postalCode());
        if (request.country() != null) address.setCountry(request.country());
        if (request.label() != null) address.setLabel(request.label());

        if (Boolean.TRUE.equals(request.isDefault()) && !address.getIsDefault()) {
            clearDefaultAddress(userId);
            address.setIsDefault(true);
        }

        Address savedAddress = addressRepository.save(address);
        log.info("Address updated: {} for user: {}", addressId, userId);
        return mapToAddressResponse(savedAddress);
    }

    @Transactional
    public void deleteAddress(Long addressId, Long userId) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Address not found with id: " + addressId));

        addressRepository.delete(address);
        log.info("Address deleted: {} for user: {}", addressId, userId);
    }

    @Transactional
    public AddressResponse setDefaultAddress(Long addressId, Long userId) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Address not found with id: " + addressId));

        clearDefaultAddress(userId);
        address.setIsDefault(true);
        Address savedAddress = addressRepository.save(address);
        log.info("Default address set: {} for user: {}", addressId, userId);
        return mapToAddressResponse(savedAddress);
    }

    private void clearDefaultAddress(Long userId) {
        addressRepository.findByUserIdAndIsDefaultTrue(userId)
                .ifPresent(existing -> {
                    existing.setIsDefault(false);
                    addressRepository.save(existing);
                });
    }

    private AddressResponse mapToAddressResponse(Address address) {
        return new AddressResponse(
                address.getId(),
                address.getUser().getId(),
                address.getLabel(),
                address.getStreet(),
                address.getCity(),
                address.getState(),
                address.getPostalCode(),
                address.getCountry(),
                address.getIsDefault(),
                address.getCreatedAt(),
                address.getUpdatedAt()
        );
    }
}
