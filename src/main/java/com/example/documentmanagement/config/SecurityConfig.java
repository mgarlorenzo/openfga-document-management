package com.example.documentmanagement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.example.documentmanagement.repository.EmployeeRepository;
import com.example.documentmanagement.security.EmployeeAuthenticationProvider;
import com.example.documentmanagement.security.EmployeeUserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final EmployeeRepository employeeRepository;

    public SecurityConfig(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/graphql").permitAll()
                .requestMatchers("/graphiql").permitAll()
                .anyRequest().authenticated()
            )
            .httpBasic();

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(EmployeeAuthenticationProvider authenticationProvider) {
        return new ProviderManager(authenticationProvider);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public EmployeeUserDetailsService userDetailsService() {
        return new EmployeeUserDetailsService(employeeRepository);
    }

    @Bean
    public EmployeeAuthenticationProvider authenticationProvider(EmployeeUserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        return new EmployeeAuthenticationProvider(userDetailsService, passwordEncoder);
    }
} 