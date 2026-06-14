package com.keyd.mmotors.security;

import com.keyd.mmotors.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final AppUserRepository appUserRepository;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/vehicles/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/vehicles").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/vehicles/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/application-files").hasRole("CLIENT")
                        .requestMatchers(HttpMethod.GET, "/api/application-files/my/**").hasRole("CLIENT")
                        .requestMatchers(HttpMethod.GET, "/api/application-files/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/application-files/admin/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/document-files/application-files/**").hasRole("CLIENT")
                        .requestMatchers(HttpMethod.GET, "/api/document-files/application-files/**").hasRole("CLIENT")
                        .requestMatchers(HttpMethod.GET, "/api/document-files/admin/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/monitoring/status").hasRole("ADMIN")
                        .requestMatchers("/actuator/health").permitAll()

                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults())
                .build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return email -> appUserRepository.findByEmail(email)
                .map(appUser -> User.builder()
                        .username(appUser.getEmail())
                        .password(appUser.getPassword())
                        .roles(appUser.getRole().name())
                        .build()
                )
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable : " + email));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
