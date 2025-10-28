package com.umdev.infoeste.services;

import com.umdev.infoeste.dto.*;
import com.umdev.infoeste.entities.Store;
import com.umdev.infoeste.entities.UserRole;
import com.umdev.infoeste.mappers.StoreMapper;
import com.umdev.infoeste.repositories.StoreRepository;
import com.umdev.infoeste.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class StoreService {
    private final StoreRepository storeRepository;
    private final StoreMapper storeMapper;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final Logger storeLogger = LoggerFactory.getLogger(StoreService.class);

    public StoreService(StoreRepository storeRepository, StoreMapper storeMapper, 
                       AuthenticationManager authenticationManager, JwtService jwtService) {
        this.storeRepository = storeRepository;
        this.storeMapper = storeMapper;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public StoreRegisterResponseDto register(StoreRegisterDto registerDto) {
        storeLogger.info("Attempting to register store with email: {}", registerDto.email());
        
        Store storeToAdd = storeMapper.toEntity(registerDto);

        if (storeRepository.existsByEmail(storeToAdd.getEmail())) {
            storeLogger.warn("Store registration failed - email already exists: {}", storeToAdd.getEmail());
            throw new IllegalArgumentException("Store already exists with email: " + storeToAdd.getEmail());
        }

        String encryptedPassword = new BCryptPasswordEncoder().encode(storeToAdd.getPassword());
        storeToAdd.setPassword(encryptedPassword);

        storeToAdd.setCreatedAt(LocalDateTime.now());
        storeToAdd.setRole(UserRole.USER);

        Store savedStore = storeRepository.save(storeToAdd);
        storeLogger.info("Store registered successfully with ID: {}", savedStore.getId());

        return storeMapper.toRegisterResponse(savedStore);
    }

    public StoreLoginResponseDto login(StoreLoginDto loginDto) {
        storeLogger.info("Attempting login for store with email: {}", loginDto.email());
        
        UsernamePasswordAuthenticationToken usernamePassword = 
            new UsernamePasswordAuthenticationToken(loginDto.email(), loginDto.password());
        Authentication auth = authenticationManager.authenticate(usernamePassword);

        String jwt = jwtService.getToken(auth.getName());

        Store store = storeRepository.findByEmail(loginDto.email())
                .orElseThrow(() -> {
                    storeLogger.warn("Login failed - store not found with email: {}", loginDto.email());
                    return new UsernameNotFoundException("Store not found with email: " + loginDto.email());
                });

        storeLogger.info("Store login successful for ID: {}", store.getId());

        StoreLoginResponseDto.StoreInfo storeInfo = storeMapper.toLoginStoreInfo(store);
        return new StoreLoginResponseDto(jwt, storeInfo);
    }

    public StoreProfileDto getProfile(String email) {
        storeLogger.info("Fetching profile for store with email: {}", email);
        
        Store store = storeRepository.findByEmail(email)
                .orElseThrow(() -> {
                    storeLogger.warn("Profile fetch failed - store not found with email: {}", email);
                    return new UsernameNotFoundException("Store not found with email: " + email);
                });

        return storeMapper.toProfileDto(store);
    }

    public PaginatedResponseDto<StorePublicDto> getStores(int page, int limit, String query) {
        storeLogger.info("Fetching stores - page: {}, limit: {}, query: {}", page, limit, query);
        
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Store> storePage = storeRepository.findStoresWithSearch(query, pageable);

        List<StorePublicDto> storeList = storePage.getContent()
                .stream()
                .map(storeMapper::toPublicDto)
                .toList();

        PaginatedResponseDto.MetaData meta = new PaginatedResponseDto.MetaData(
                page, 
                limit, 
                storePage.getTotalElements()
        );

        return new PaginatedResponseDto<>(meta, storeList);
    }

    public StoreDetailDto getStoreById(UUID storeId) {
        storeLogger.info("Fetching store details for ID: {}", storeId);
        
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> {
                    storeLogger.warn("Store not found with ID: {}", storeId);
                    return new IllegalArgumentException("Store not found with id: " + storeId);
                });

        return storeMapper.toDetailDto(store);
    }

    public UUID getStoreIdByEmail(String email) {
        Store store = storeRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Store not found with email: " + email));
        return store.getId();
    }
}
