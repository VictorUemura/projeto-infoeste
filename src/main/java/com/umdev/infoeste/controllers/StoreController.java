package com.umdev.infoeste.controllers;

import com.umdev.infoeste.dto.*;
import com.umdev.infoeste.services.StoreService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/stores")
@CrossOrigin(origins = "*", maxAge = 3600)
public class StoreController {

    private final StoreService storeService;

    public StoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    @PostMapping("/register")
    public ResponseEntity<StoreRegisterResponseDto> register(@Valid @RequestBody StoreRegisterDto registerDto) {
        StoreRegisterResponseDto response = storeService.register(registerDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<StoreLoginResponseDto> login(@Valid @RequestBody StoreLoginDto loginDto) {
        StoreLoginResponseDto response = storeService.login(loginDto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<StoreProfileDto> getProfile(Authentication authentication) {
        String email = authentication.getName();
        StoreProfileDto profile = storeService.getProfile(email);
        return ResponseEntity.ok(profile);
    }

    @GetMapping
    public ResponseEntity<PaginatedResponseDto<StorePublicDto>> getStores(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String q) {
        
        PaginatedResponseDto<StorePublicDto> response = storeService.getStores(page, limit, q);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{storeId}")
    public ResponseEntity<StoreDetailDto> getStoreById(@PathVariable UUID storeId) {
        StoreDetailDto storeDetail = storeService.getStoreById(storeId);
        return ResponseEntity.ok(storeDetail);
    }
}