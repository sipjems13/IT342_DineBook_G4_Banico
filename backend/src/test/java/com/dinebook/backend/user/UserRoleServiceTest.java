package com.dinebook.backend.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserRoleService Unit Tests")
class UserRoleServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private UserRoleService userRoleService;

    private AppUser existingUser;

    @BeforeEach
    void setUp() {
        existingUser = new AppUser();
        existingUser.setEmail("diner@example.com");
        existingUser.setRole(UserRole.DINER);
    }

    @Test
    @DisplayName("TC-USER-01: resolveRole returns DINER for existing diner user")
    void resolveRole_existingDiner_returnsDiner() {
        when(appUserRepository.findByEmailIgnoreCase("diner@example.com"))
                .thenReturn(Optional.of(existingUser));

        UserRole role = userRoleService.resolveRole("diner@example.com");

        assertThat(role).isEqualTo(UserRole.DINER);
    }

    @Test
    @DisplayName("TC-USER-02: resolveRole returns DINER when user not found")
    void resolveRole_userNotFound_returnsDiner() {
        when(appUserRepository.findByEmailIgnoreCase(anyString()))
                .thenReturn(Optional.empty());

        UserRole role = userRoleService.resolveRole("unknown@example.com");

        assertThat(role).isEqualTo(UserRole.DINER);
    }

    @Test
    @DisplayName("TC-USER-03: resolveRole returns STAFF for staff user")
    void resolveRole_staffUser_returnsStaff() {
        AppUser staffUser = new AppUser();
        staffUser.setEmail("staff@example.com");
        staffUser.setRole(UserRole.STAFF);

        when(appUserRepository.findByEmailIgnoreCase("staff@example.com"))
                .thenReturn(Optional.of(staffUser));

        UserRole role = userRoleService.resolveRole("staff@example.com");

        assertThat(role).isEqualTo(UserRole.STAFF);
    }

    @Test
    @DisplayName("TC-USER-04: ensureUser returns existing user without creating new one")
    void ensureUser_existingUser_returnsExisting() {
        when(appUserRepository.findByEmailIgnoreCase("diner@example.com"))
                .thenReturn(Optional.of(existingUser));

        AppUser result = userRoleService.ensureUser("diner@example.com");

        assertThat(result.getEmail()).isEqualTo("diner@example.com");
        verify(appUserRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC-USER-05: ensureUser creates new DINER user when not found")
    void ensureUser_newUser_createsWithDinerRole() {
        when(appUserRepository.findByEmailIgnoreCase("new@example.com"))
                .thenReturn(Optional.empty());
        when(appUserRepository.save(any(AppUser.class))).thenAnswer(inv -> inv.getArgument(0));

        AppUser result = userRoleService.ensureUser("new@example.com");

        assertThat(result.getEmail()).isEqualTo("new@example.com");
        assertThat(result.getRole()).isEqualTo(UserRole.DINER);
        verify(appUserRepository).save(any(AppUser.class));
    }
}
