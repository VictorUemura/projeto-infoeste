package com.umdev.infoeste.services;

import com.umdev.infoeste.repositories.StoreRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final StoreRepository storeRepository;

    public UserDetailsServiceImpl(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return storeRepository.findByEmail(username).
                orElseThrow(() -> new UsernameNotFoundException(username));
    }
}

