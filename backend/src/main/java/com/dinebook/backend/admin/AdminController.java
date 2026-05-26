package com.dinebook.backend.admin;

import com.dinebook.backend.admin.dto.AdminUserDto;
import com.dinebook.backend.booking.RequestStatus;
import com.dinebook.backend.booking.dto.DiningRequestDto;
import com.dinebook.backend.user.CurrentUserService;
import com.dinebook.backend.user.UserRole;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;
    private final CurrentUserService currentUserService;

    public AdminController(AdminService adminService, CurrentUserService currentUserService) {
        this.adminService = adminService;
        this.currentUserService = currentUserService;
    }

    // ── One-time bootstrap (no admin auth required – only works when 0 admins exist) ──

    @PostMapping("/bootstrap")
    public ResponseEntity<AdminUserDto> bootstrap(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        return ResponseEntity.ok(adminService.bootstrapAdmin(email));
    }

    // ── Requests ─────────────────────────────────────────────────────────────

    @GetMapping("/requests")
    public List<DiningRequestDto> allRequests(Authentication authentication) {
        currentUserService.requireAdmin(authentication);
        return adminService.getAllRequests();
    }

    @PatchMapping("/requests/{id}/status")
    public DiningRequestDto updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        currentUserService.requireAdmin(authentication);
        RequestStatus status = RequestStatus.valueOf(body.get("status").toUpperCase());
        return adminService.updateRequestStatus(id, status);
    }

    @DeleteMapping("/requests/{id}")
    public ResponseEntity<Void> deleteRequest(
            @PathVariable Long id,
            Authentication authentication) {
        currentUserService.requireAdmin(authentication);
        adminService.deleteRequest(id);
        return ResponseEntity.noContent().build();
    }

    // ── Users ─────────────────────────────────────────────────────────────────

    @GetMapping("/users")
    public List<AdminUserDto> allUsers(Authentication authentication) {
        currentUserService.requireAdmin(authentication);
        return adminService.getAllUsers();
    }

    @PatchMapping("/users/{id}/role")
    public AdminUserDto updateUserRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        currentUserService.requireAdmin(authentication);
        UserRole newRole = UserRole.valueOf(body.get("role").toUpperCase());
        return adminService.updateUserRole(id, newRole);
    }

    @PostMapping("/users")
    public ResponseEntity<AdminUserDto> createUser(
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        currentUserService.requireAdmin(authentication);
        String email = body.get("email");
        String password = body.get("password");
        UserRole role = UserRole.valueOf(body.get("role").toUpperCase());
        return ResponseEntity.ok(adminService.createUser(email, password, role));
    }
}
