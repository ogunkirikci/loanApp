package com.example.loanapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails admin = User.builder()
            .username("admin")
            .password(passwordEncoder().encode("admin123"))
            .roles("ADMIN")
            .build();
            
        UserDetails customer = User.builder()
            .username("customer")
            .password(passwordEncoder().encode("customer123"))
            .roles("CUSTOMER")
            .build();
            
        return new InMemoryUserDetailsManager(admin, customer);
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf((csrf) -> csrf.disable())
            .authorizeHttpRequests((authz) -> authz
                .requestMatchers(new AntPathRequestMatcher("/h2-console/**")).permitAll()
                .anyRequest().authenticated()
            )
            .httpBasic((basic) -> basic
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setContentType("application/json");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"Authentication is required to access this resource\"}");
                })
            )
            .headers((headers) -> headers
                .frameOptions((frame) -> frame.disable())
            );
        
        return http.build();
    }
}