package com.dinebook.backend.admin;

import com.dinebook.backend.admin.dto.AdminUserDto;
import com.dinebook.backend.auth.adapter.AuthClient;
import com.dinebook.backend.auth.dto.RegisterRequest;
import com.dinebook.backend.booking.DiningRequest;
import com.dinebook.backend.booking.DiningRequestRepository;
import com.dinebook.backend.booking.RequestStatus;
import com.dinebook.backend.booking.dto.DiningRequestDto;
import com.dinebook.backend.user.AppUser;
import com.dinebook.backend.user.AppUserRepository;
import com.dinebook.backend.user.UserRole;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class AdminService {

    private final DiningRequestRepository diningRequestRepository;
    private final AppUserRepository appUserRepository;
    private final AuthClient authClient;

    public AdminService(DiningRequestRepository diningRequestRepository,
                        AppUserRepository appUserRepository,
                        AuthClient authClient) {
        this.diningRequestRepository = diningRequestRepository;
        this.appUserRepository = appUserRepository;
        this.authClient = authClient;
    }

    /** Return every dining request (all users, all restaurants). */
    public List<DiningRequestDto> getAllRequests() {
        return diningRequestRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toDto)
                .toList();
    }

    /** Update the status of any dining request. */
    @Transactional
    public DiningRequestDto updateRequestStatus(Long requestId, RequestStatus status) {
        if (status == RequestStatus.PENDING) {
            throw new ResponseStatusException(BAD_REQUEST, "Cannot set status back to PENDING");
        }
        DiningRequest req = diningRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Dining request not found"));
        req.setStatus(status);
        return toDto(diningRequestRepository.save(req));
    }

    /** Return all registered users. */
    public List<AdminUserDto> getAllUsers() {
        return appUserRepository.findAll()
                .stream()
                .map(u -> new AdminUserDto(u.getId(), u.getEmail(), u.getRole().name()))
                .toList();
    }

    /** Change a user's role (e.g. promote to ADMIN, demote to DINER). */
    @Transactional
    public AdminUserDto updateUserRole(Long userId, UserRole newRole) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
        user.setRole(newRole);
        AppUser saved = appUserRepository.save(user);
        return new AdminUserDto(saved.getId(), saved.getEmail(), saved.getRole().name());
    }

    /**
     * Bootstrap: promote the given email to ADMIN only if no ADMIN exists yet.
     * After the first admin exists this endpoint becomes a no-op / throws.
     */
    @Transactional
    public AdminUserDto bootstrapAdmin(String email) {
        long adminCount = appUserRepository.countByRole(UserRole.ADMIN);
        if (adminCount > 0) {
            throw new ResponseStatusException(BAD_REQUEST,
                    "An admin account already exists. Use the Admin Panel to promote additional users.");
        }
        AppUser user = appUserRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND,
                        "User not found. Register first, then call this endpoint."));
        user.setRole(UserRole.ADMIN);
        AppUser saved = appUserRepository.save(user);
        return new AdminUserDto(saved.getId(), saved.getEmail(), saved.getRole().name());
    }

    /** Manually create a user and set their role. */
    @Transactional
    public AdminUserDto createUser(String email, String password, UserRole role) {
        // 1. Call Supabase to register the user
        ResponseEntity<?> response = authClient.registerUser(new RegisterRequest(email, password, ""));
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new ResponseStatusException(response.getStatusCode(), "Supabase registration failed");
        }

        // 2. Ensure user exists in our local DB and set the requested role
        AppUser user = appUserRepository.findByEmailIgnoreCase(email)
                .orElseGet(() -> {
                    AppUser u = new AppUser();
                    u.setEmail(email);
                    return u;
                });
        user.setRole(role);
        AppUser saved = appUserRepository.save(user);
        return new AdminUserDto(saved.getId(), saved.getEmail(), saved.getRole().name());
    }

    /** Delete a dining request. */
    @Transactional
    public void deleteRequest(Long id) {
        if (!diningRequestRepository.existsById(id)) {
            throw new ResponseStatusException(NOT_FOUND, "Dining request not found");
        }
        diningRequestRepository.deleteById(id);
    }

    // ── helpers ─────────────────────────────────────────────────────────────

    private DiningRequestDto toDto(DiningRequest e) {
        return new DiningRequestDto(
                e.getId(),
                e.getRestaurant().getId(),
                e.getRestaurant().getName(),
                e.getDinerEmail(),
                e.getRequestedDateTime(),
                e.getGuests(),
                e.getStatus(),
                e.getCreatedAt()
        );
    }
}
