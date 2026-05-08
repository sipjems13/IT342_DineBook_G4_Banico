package com.dinebook.backend.booking;

import com.dinebook.backend.booking.dto.CreateDiningRequest;
import com.dinebook.backend.booking.dto.DiningRequestDto;
import com.dinebook.backend.restaurant.Restaurant;
import com.dinebook.backend.restaurant.RestaurantService;
import com.dinebook.backend.shared.notification.Notification;
import com.dinebook.backend.shared.notification.NotificationFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("DiningRequestService Unit Tests")
class DiningRequestServiceTest {

    @Mock
    private DiningRequestRepository diningRequestRepository;

    @Mock
    private RestaurantService restaurantService;

    @Mock
    private NotificationFactory notificationFactory;

    @Mock
    private Notification notification;

    @InjectMocks
    private DiningRequestService diningRequestService;

    private Restaurant restaurant;
    private DiningRequest pendingRequest;

    @BeforeEach
    void setUp() {
        restaurant = new Restaurant();
        restaurant.setName("Cebu Lechon House");
        restaurant.setLocation("Cebu City");
        restaurant.setCuisine("Filipino");
        restaurant.setRating(4.5);

        pendingRequest = new DiningRequest();
        pendingRequest.setRestaurant(restaurant);
        pendingRequest.setDinerEmail("diner@example.com");
        pendingRequest.setRequestedDateTime(LocalDateTime.now().plusDays(1));
        pendingRequest.setGuests(2);
        pendingRequest.setStatus(RequestStatus.PENDING);

        when(notificationFactory.createNotification("EMAIL")).thenReturn(notification);
        doNothing().when(notification).send(anyString(), anyString());
    }

    @Test
    @DisplayName("TC-BOOK-01: create saves dining request and sends notification")
    void create_validRequest_savesAndNotifies() {
        when(restaurantService.findById(1L)).thenReturn(restaurant);
        when(diningRequestRepository.save(any(DiningRequest.class))).thenReturn(pendingRequest);

        CreateDiningRequest req = new CreateDiningRequest(
                1L, LocalDateTime.now().plusDays(1), 2);

        DiningRequestDto result = diningRequestService.create("diner@example.com", req);

        assertThat(result.dinerEmail()).isEqualTo("diner@example.com");
        assertThat(result.status()).isEqualTo(RequestStatus.PENDING);
        verify(notification).send(eq("diner@example.com"), anyString());
    }

    @Test
    @DisplayName("TC-BOOK-02: myRequests returns requests for given email")
    void myRequests_validEmail_returnsList() {
        when(diningRequestRepository.findByDinerEmailIgnoreCaseOrderByCreatedAtDesc("diner@example.com"))
                .thenReturn(List.of(pendingRequest));

        List<DiningRequestDto> result = diningRequestService.myRequests("diner@example.com");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).dinerEmail()).isEqualTo("diner@example.com");
    }

    @Test
    @DisplayName("TC-BOOK-03: updateStatus to APPROVED succeeds")
    void updateStatus_toApproved_succeeds() {
        when(diningRequestRepository.findById(1L)).thenReturn(Optional.of(pendingRequest));
        when(diningRequestRepository.save(any(DiningRequest.class))).thenReturn(pendingRequest);

        DiningRequestDto result = diningRequestService.updateStatus(1L, RequestStatus.APPROVED);

        assertThat(result.status()).isEqualTo(RequestStatus.APPROVED);
        verify(notification).send(anyString(), anyString());
    }

    @Test
    @DisplayName("TC-BOOK-04: updateStatus to PENDING throws BAD_REQUEST")
    void updateStatus_toPending_throwsBadRequest() {
        assertThatThrownBy(() -> diningRequestService.updateStatus(1L, RequestStatus.PENDING))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Cannot set request back to PENDING");
    }

    @Test
    @DisplayName("TC-BOOK-05: updateStatus throws 404 when request not found")
    void updateStatus_notFound_throws404() {
        when(diningRequestRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> diningRequestService.updateStatus(99L, RequestStatus.APPROVED))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Dining request not found");
    }

    @Test
    @DisplayName("TC-BOOK-06: allRequests returns all dining requests")
    void allRequests_returnsAll() {
        when(diningRequestRepository.findAll()).thenReturn(List.of(pendingRequest));

        List<DiningRequestDto> result = diningRequestService.allRequests();

        assertThat(result).hasSize(1);
    }
}
