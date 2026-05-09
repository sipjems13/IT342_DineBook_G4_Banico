package com.dinebook.backend.staff;

import com.dinebook.backend.booking.DiningRequestService;
import com.dinebook.backend.booking.RequestStatus;
import com.dinebook.backend.booking.dto.DiningRequestDto;
import com.dinebook.backend.booking.dto.UpdateDiningRequestStatus;
import com.dinebook.backend.restaurant.RestaurantService;
import com.dinebook.backend.restaurant.dto.RestaurantDto;
import com.dinebook.backend.restaurant.dto.RestaurantUpsertRequest;
import com.dinebook.backend.user.CurrentUserService;
import com.dinebook.backend.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.FORBIDDEN;

@ExtendWith(MockitoExtension.class)
@DisplayName("StaffController Unit Tests")
class StaffControllerTest {

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private RestaurantService restaurantService;

    @Mock
    private DiningRequestService diningRequestService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private StaffController staffController;

    private RestaurantDto sampleRestaurantDto;
    private DiningRequestDto sampleRequestDto;

    @BeforeEach
    void setUp() {
        sampleRestaurantDto = new RestaurantDto(1L, "Cebu Lechon House", "Cebu City", "Filipino", null, 4.5);
        sampleRequestDto = new DiningRequestDto(
                1L, 1L, "Cebu Lechon House",
                "diner@example.com",
                LocalDateTime.now().plusDays(1),
                4, RequestStatus.PENDING, LocalDateTime.now()
        );
    }

    // ─── createRestaurant ────────────────────────────────────────────────────

    @Test
    @DisplayName("TC-STAFF-01: createRestaurant succeeds when caller is STAFF")
    void createRestaurant_staffRole_returnsDto() {
        RestaurantUpsertRequest request = new RestaurantUpsertRequest(
                "Cebu Lechon House", "Cebu City", "Filipino", null);
        when(restaurantService.create(request)).thenReturn(sampleRestaurantDto);

        RestaurantDto result = staffController.createRestaurant(request, authentication);

        assertThat(result.name()).isEqualTo("Cebu Lechon House");
        verify(currentUserService).requireStaff(authentication);
        verify(restaurantService).create(request);
    }

    @Test
    @DisplayName("TC-STAFF-02: createRestaurant throws FORBIDDEN when caller is not STAFF")
    void createRestaurant_dinerRole_throwsForbidden() {
        RestaurantUpsertRequest request = new RestaurantUpsertRequest(
                "Cebu Lechon House", "Cebu City", "Filipino", null);
        doThrow(new ResponseStatusException(FORBIDDEN, "Staff role required"))
                .when(currentUserService).requireStaff(authentication);

        assertThatThrownBy(() -> staffController.createRestaurant(request, authentication))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Staff role required");
        verify(restaurantService, never()).create(any());
    }

    // ─── updateRestaurant ────────────────────────────────────────────────────

    @Test
    @DisplayName("TC-STAFF-03: updateRestaurant succeeds when caller is STAFF")
    void updateRestaurant_staffRole_returnsDto() {
        RestaurantUpsertRequest request = new RestaurantUpsertRequest(
                "Updated Name", "Cebu City", "Fusion", null);
        RestaurantDto updated = new RestaurantDto(1L, "Updated Name", "Cebu City", "Fusion", null, 4.5);
        when(restaurantService.update(1L, request)).thenReturn(updated);

        RestaurantDto result = staffController.updateRestaurant(1L, request, authentication);

        assertThat(result.name()).isEqualTo("Updated Name");
        verify(currentUserService).requireStaff(authentication);
        verify(restaurantService).update(1L, request);
    }

    @Test
    @DisplayName("TC-STAFF-04: updateRestaurant throws FORBIDDEN when caller is not STAFF")
    void updateRestaurant_dinerRole_throwsForbidden() {
        RestaurantUpsertRequest request = new RestaurantUpsertRequest(
                "Updated Name", "Cebu City", "Fusion", null);
        doThrow(new ResponseStatusException(FORBIDDEN, "Staff role required"))
                .when(currentUserService).requireStaff(authentication);

        assertThatThrownBy(() -> staffController.updateRestaurant(1L, request, authentication))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Staff role required");
        verify(restaurantService, never()).update(any(), any());
    }

    // ─── deleteRestaurant ────────────────────────────────────────────────────

    @Test
    @DisplayName("TC-STAFF-05: deleteRestaurant succeeds when caller is STAFF")
    void deleteRestaurant_staffRole_invokesDelete() {
        staffController.deleteRestaurant(1L, authentication);

        verify(currentUserService).requireStaff(authentication);
        verify(restaurantService).delete(1L);
    }

    @Test
    @DisplayName("TC-STAFF-06: deleteRestaurant throws FORBIDDEN when caller is not STAFF")
    void deleteRestaurant_dinerRole_throwsForbidden() {
        doThrow(new ResponseStatusException(FORBIDDEN, "Staff role required"))
                .when(currentUserService).requireStaff(authentication);

        assertThatThrownBy(() -> staffController.deleteRestaurant(1L, authentication))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Staff role required");
        verify(restaurantService, never()).delete(any());
    }

    // ─── getAllRequests ───────────────────────────────────────────────────────

    @Test
    @DisplayName("TC-STAFF-07: getAllRequests returns all dining requests for STAFF")
    void getAllRequests_staffRole_returnsList() {
        when(diningRequestService.allRequests()).thenReturn(List.of(sampleRequestDto));

        List<DiningRequestDto> result = staffController.getAllRequests(authentication);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).dinerEmail()).isEqualTo("diner@example.com");
        verify(currentUserService).requireStaff(authentication);
    }

    @Test
    @DisplayName("TC-STAFF-08: getAllRequests throws FORBIDDEN when caller is not STAFF")
    void getAllRequests_dinerRole_throwsForbidden() {
        doThrow(new ResponseStatusException(FORBIDDEN, "Staff role required"))
                .when(currentUserService).requireStaff(authentication);

        assertThatThrownBy(() -> staffController.getAllRequests(authentication))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Staff role required");
        verify(diningRequestService, never()).allRequests();
    }

    // ─── updateStatus ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("TC-STAFF-09: updateStatus to APPROVED succeeds for STAFF")
    void updateStatus_toApproved_staffRole_succeeds() {
        UpdateDiningRequestStatus updateReq = new UpdateDiningRequestStatus(RequestStatus.APPROVED);
        DiningRequestDto approved = new DiningRequestDto(
                1L, 1L, "Cebu Lechon House", "diner@example.com",
                LocalDateTime.now().plusDays(1), 4, RequestStatus.APPROVED, LocalDateTime.now()
        );
        when(diningRequestService.updateStatus(1L, RequestStatus.APPROVED)).thenReturn(approved);

        DiningRequestDto result = staffController.updateStatus(1L, updateReq, authentication);

        assertThat(result.status()).isEqualTo(RequestStatus.APPROVED);
        verify(currentUserService).requireStaff(authentication);
        verify(diningRequestService).updateStatus(1L, RequestStatus.APPROVED);
    }

    @Test
    @DisplayName("TC-STAFF-10: updateStatus throws FORBIDDEN when caller is not STAFF")
    void updateStatus_dinerRole_throwsForbidden() {
        UpdateDiningRequestStatus updateReq = new UpdateDiningRequestStatus(RequestStatus.APPROVED);
        doThrow(new ResponseStatusException(FORBIDDEN, "Staff role required"))
                .when(currentUserService).requireStaff(authentication);

        assertThatThrownBy(() -> staffController.updateStatus(1L, updateReq, authentication))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Staff role required");
        verify(diningRequestService, never()).updateStatus(any(), any());
    }
}
