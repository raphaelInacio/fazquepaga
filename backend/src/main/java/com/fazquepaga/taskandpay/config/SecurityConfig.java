package com.fazquepaga.taskandpay.config;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable) // Disable CSRF for stateless API
                .cors(withDefaults()) // Enable CORS using the configuration from WebMvcConfigurer
                .authorizeHttpRequests(
                        authorize ->
                                authorize
                                        .anyRequest()
                                        .permitAll() // Allow all requests without authentication
                        )
                .formLogin(AbstractHttpConfigurer::disable) // Disable Form Login
                .httpBasic(AbstractHttpConfigurer::disable); // Disable HTTP Basic

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
