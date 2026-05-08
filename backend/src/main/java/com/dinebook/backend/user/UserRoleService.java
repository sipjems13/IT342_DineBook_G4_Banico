package com.dinebook.backend.user;

import org.springframework.stereotype.Service;

@Service
public class UserRoleService {
    private final AppUserRepository appUserRepository;

    public UserRoleService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    public UserRole resolveRole(String email) {
        return appUserRepository.findByEmailIgnoreCase(email)
                .map(AppUser::getRole)
                .orElse(UserRole.DINER);
    }

    public AppUser ensureUser(String email) {
        return appUserRepository.findByEmailIgnoreCase(email)
                .orElseGet(() -> {
                    AppUser user = new AppUser();
                    user.setEmail(email);
                    user.setRole(UserRole.DINER);
                    return appUserRepository.save(user);
                });
    }
}
