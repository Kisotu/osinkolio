package com.musicstore.user.api.v1.controller;

import com.musicstore.user.api.v1.dto.AddressRequest;
import com.musicstore.user.api.v1.dto.AddressResponse;
import com.musicstore.user.security.UserPrincipal;
import com.musicstore.user.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/addresses")
@RequiredArgsConstructor
@Tag(name = "Address Management", description = "Endpoints for user address management")
public class AddressController {

    private final AddressService addressService;

    @Operation(summary = "Get user addresses", description = "Retrieves all addresses for the authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of addresses returned")
    })
    @GetMapping
    public ResponseEntity<List<AddressResponse>> getMyAddresses(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(addressService.getAddressesByUserId(principal.userId()));
    }

    @Operation(summary = "Get address by ID", description = "Retrieves a specific address by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Address found"),
            @ApiResponse(responseCode = "404", description = "Address not found")
    })
    @GetMapping("/{addressId}")
    public ResponseEntity<AddressResponse> getAddressById(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long addressId) {
        return ResponseEntity.ok(addressService.getAddressById(addressId, principal.userId()));
    }

    @Operation(summary = "Create address", description = "Creates a new address for the authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Address created"),
            @ApiResponse(responseCode = "400", description = "Invalid address data")
    })
    @PostMapping
    public ResponseEntity<AddressResponse> createAddress(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AddressRequest request) {
        AddressResponse response = addressService.createAddress(principal.userId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Update address", description = "Updates an existing address")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Address updated"),
            @ApiResponse(responseCode = "404", description = "Address not found")
    })
    @PutMapping("/{addressId}")
    public ResponseEntity<AddressResponse> updateAddress(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long addressId,
            @Valid @RequestBody AddressRequest request) {
        return ResponseEntity.ok(addressService.updateAddress(addressId, principal.userId(), request));
    }

    @Operation(summary = "Delete address", description = "Deletes an address")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Address deleted"),
            @ApiResponse(responseCode = "404", description = "Address not found")
    })
    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> deleteAddress(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long addressId) {
        addressService.deleteAddress(addressId, principal.userId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Set default address", description = "Sets an address as the default for the user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Default address set"),
            @ApiResponse(responseCode = "404", description = "Address not found")
    })
    @PutMapping("/{addressId}/default")
    public ResponseEntity<AddressResponse> setDefaultAddress(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long addressId) {
        return ResponseEntity.ok(addressService.setDefaultAddress(addressId, principal.userId()));
    }
}
