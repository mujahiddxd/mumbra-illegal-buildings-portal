package com.mumbra.illegalbuildings.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    UserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors().and()
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED).and()
            .authorizeRequests()
                .antMatchers(
                    "/",
                    "/index.html",
                    "/favicon.ico",
                    "/**/*.png",
                    "/**/*.gif",
                    "/**/*.svg",
                    "/**/*.jpg",
                    "/**/*.html",
                    "/**/*.css",
                    "/**/*.js",
                    "/**/*.csv"
                ).permitAll()
                .antMatchers("/api/auth/admin/login").permitAll()
                .antMatchers("/api/auth/admin/create").permitAll()
                .antMatchers("/api/auth/admin/reset").permitAll()
                .antMatchers("/api/auth/admin/debug").permitAll()
                .antMatchers("/api/auth/admin/check").authenticated()  // Require auth for check endpoint
                .antMatchers("/api/public/**").permitAll()
                .antMatchers("/api/buildings/*/approve").authenticated()  // Protect approval endpoints
                .antMatchers("/api/buildings/*/reject").authenticated()   // Protect rejection endpoints
                .antMatchers("/api/buildings/all").authenticated()        // Protect admin view all endpoint
                .antMatchers("/api/buildings/export/**").authenticated()  // Protect export endpoints
                .antMatchers("/api/buildings/**").permitAll()             // Allow public access to other buildings API
                .antMatchers("/api/sse/**").permitAll()                   // Allow public access to SSE endpoints
                .anyRequest().authenticated();

        return http.build();
    }
}