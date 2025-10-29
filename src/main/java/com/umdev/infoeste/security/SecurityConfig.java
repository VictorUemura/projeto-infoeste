package com.umdev.infoeste.security;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    private final AuthenticatorFilter authenticatorFilter;
    private final AuthEntryPoint exceptionHandling;
    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(AuthenticatorFilter authenticatorFilter, 
                         AuthEntryPoint exceptionHandling,
                         @Qualifier("corsConfigurationSource") CorsConfigurationSource corsConfigurationSource) {
        this.authenticatorFilter = authenticatorFilter;
        this.exceptionHandling = exceptionHandling;
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        // Permitir todas as requisições OPTIONS (preflight)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        
                        // Swagger e documentação
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/swagger-resources/**", 
                                       "/v3/api-docs/**", "/v3/api-docs", "/swagger-ui/index.html", "/webjars/**").permitAll()
                        
                        // Endpoints de erro
                        .requestMatchers("/error").permitAll()
                     
                        .requestMatchers(HttpMethod.POST, "/v1/stores/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/v1/stores/register").permitAll()
                        
                        .anyRequest().authenticated())
                .addFilterBefore(authenticatorFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(this.exceptionHandling));
        
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
