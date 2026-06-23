package com.keyd.mmotors.security;

import com.keyd.mmotors.entity.AppUser;
import com.keyd.mmotors.entity.Role;
import com.keyd.mmotors.repository.AppUserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Test
    @DisplayName("Doit charger un utilisateur depuis son email")
    void shouldLoadUserByEmail() {
        AppUser admin = AppUser.builder()
                .firstName("Admin")
                .lastName("M-Motors")
                .email("admin@mmotors.demo")
                .password("$2a$10$encodedPassword")
                .role(Role.ADMIN)
                .build();

        when(appUserRepository.findByEmail("admin@mmotors.demo"))
                .thenReturn(Optional.of(admin));

        SecurityConfig securityConfig = new SecurityConfig(appUserRepository);

        UserDetails userDetails = securityConfig
                .userDetailsService()
                .loadUserByUsername("admin@mmotors.demo");

        assertThat(userDetails.getUsername()).isEqualTo("admin@mmotors.demo");
        assertThat(userDetails.getPassword()).isEqualTo("$2a$10$encodedPassword");
        assertThat(userDetails.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .contains("ROLE_ADMIN");
    }

    @Test
    @DisplayName("Doit retourner une erreur si l'utilisateur est introuvable")
    void shouldThrowExceptionWhenUserDoesNotExist() {
        when(appUserRepository.findByEmail("unknown@mmotors.demo"))
                .thenReturn(Optional.empty());

        SecurityConfig securityConfig = new SecurityConfig(appUserRepository);

        assertThatThrownBy(() -> securityConfig
                .userDetailsService()
                .loadUserByUsername("unknown@mmotors.demo"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Utilisateur introuvable");
    }

    @Test
    @DisplayName("Doit encoder un mot de passe avec BCrypt")
    void shouldEncodePasswordWithBCrypt() {
        SecurityConfig securityConfig = new SecurityConfig(appUserRepository);

        String encodedPassword = securityConfig
                .passwordEncoder()
                .encode("Admin123!");

        assertThat(encodedPassword).isNotEqualTo("Admin123!");
        assertThat(securityConfig.passwordEncoder().matches("Admin123!", encodedPassword)).isTrue();
    }
}
