package com.crime.reporting_system;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.crime.reporting_system.repository.UserRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserRepository userRepository;
    private final AuthenticationSuccessHandler authenticationSuccessHandler;
    private final AuthenticationFailureHandler authenticationFailureHandler;

    public SecurityConfig(UserRepository userRepository,
                          AuthenticationSuccessHandler authenticationSuccessHandler,
                          AuthenticationFailureHandler authenticationFailureHandler) {
        this.userRepository = userRepository;
        this.authenticationSuccessHandler = authenticationSuccessHandler;
        this.authenticationFailureHandler = authenticationFailureHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable()) // Disable for development; enable in production
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/api/user/register").permitAll()
                        .requestMatchers("/api/reports").hasAnyRole("CITIZEN", "POLICE")
                        .requestMatchers("/api/reports/all").hasRole("CITIZEN")
                        .requestMatchers("/api/cases/**", "/api/reports/pending", "/api/reports/rejected", "/api/reports/officers").hasRole("POLICE")
                        .requestMatchers("/api/user/**").hasAnyRole("CITIZEN", "POLICE")
                        .anyRequest().authenticated()
                )
                .formLogin((form) -> form
                        .loginProcessingUrl("/login")
                        .successHandler(authenticationSuccessHandler)
                        .failureHandler(authenticationFailureHandler)
                        .permitAll()
                )
                .logout((logout) -> logout
                        .logoutUrl("/api/user/logout") // Match your custom logout endpoint
                        .invalidateHttpSession(true)  // Invalidate session
                        .clearAuthentication(true)   // Clear authentication
                        .permitAll()                 // Allow logout for all
                )
                .exceptionHandling((exception) -> exception
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                response.setStatus(403))
                        .authenticationEntryPoint((request, response, authException) -> {
                            System.out.println("Authentication entry point triggered: " + authException.getMessage());
                            response.sendError(401, "Unauthorized");
                        })
                );
        return http.build();
    }

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
//        config.addAllowedOrigin("http://localhost:5173");
        config.addAllowedOrigin("https://crime-report-system-frontend.onrender.com");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            com.crime.reporting_system.entity.User user = userRepository.findByUsername(username);
            if (user == null) {
                throw new UsernameNotFoundException("User not found");
            }
            String role = user.getRole();
            System.out.println("Loading user: " + username + ", role: " + role + ", authority: ROLE_" + role);
            return new org.springframework.security.core.userdetails.User(
                    user.getUsername(),
                    user.getPassword(),
                    org.springframework.security.core.authority.AuthorityUtils.createAuthorityList("ROLE_" + role)
            );
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}