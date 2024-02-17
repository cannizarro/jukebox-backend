package com.cannizarro.jukebox.config.security;

import com.cannizarro.jukebox.config.repository.UserRepository;
import com.cannizarro.jukebox.config.security.jwt.JWTAuthenticationFilter;
import com.cannizarro.jukebox.config.security.jwt.JwtAuthEntryPoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    @Value("${restaurant.origin}")
    private String restaurantOrigin;

    private final UserRepository userRepository;
    private final JwtAuthEntryPoint authEntryPoint;

    public SecurityConfig(UserRepository userRepository, JwtAuthEntryPoint authEntryPoint) {
        this.userRepository = userRepository;
        this.authEntryPoint = authEntryPoint;
    }

    @Bean
    public ReactiveUserDetailsService userDetailsService() throws UsernameNotFoundException {
        return username -> userRepository
                    .getUser(username)
                    .map(user -> user);
    }

    @Bean
    public JWTAuthenticationFilter jwtAuthenticationFilter() {
        return new JWTAuthenticationFilter();
    }

    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {
        http.authorizeExchange(exchange ->
                exchange
                    .pathMatchers("/actuator/**").permitAll()
                    .pathMatchers("/public/**").permitAll()
                    .pathMatchers("/customer/**").permitAll()
                    .pathMatchers( "/admin/**").hasAuthority("ROLE_ADMIN")
                    .pathMatchers( "/ws/state/**").hasAuthority("ROLE_ADMIN")
                    .anyExchange()
                    .authenticated()
        );
        http.cors(corsSpec -> corsSpec.configurationSource(getCorsConfigurationSource()));
        http.httpBasic(Customizer.withDefaults());
        http.exceptionHandling(configurer -> configurer.authenticationEntryPoint(authEntryPoint));
        http.csrf(ServerHttpSecurity.CsrfSpec::disable);
        http.securityContextRepository(NoOpServerSecurityContextRepository.getInstance());
        http.addFilterBefore(jwtAuthenticationFilter(), SecurityWebFiltersOrder.AUTHENTICATION);
        return http.build();
    }

    @Bean
    CorsConfigurationSource getCorsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(restaurantOrigin));
        configuration.setAllowedMethods(List.of("GET", "DELETE", "PUT", "POST"));
        configuration.setAllowCredentials(true);
        configuration.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
