package com.umdev.infoeste.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class AuthenticatorFilter extends OncePerRequestFilter {
    private final JwtService jwtService;

    public AuthenticatorFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // Sempre permitir requisições OPTIONS (CORS preflight)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String jws = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (jws == null || !jws.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String user = jwtService.getAuthUser(request);
            if (user != null) {
                Authentication authentication =
                        new UsernamePasswordAuthenticationToken(user, null,
                                Collections.emptyList());

                SecurityContextHolder.getContext()
                        .setAuthentication(authentication);
            }

        } catch (Exception e) {
            logger.warn("Tentativa de autenticação falhou com token inválido: " + e.getMessage());
        }
        filterChain.doFilter(request, response);
    }
}