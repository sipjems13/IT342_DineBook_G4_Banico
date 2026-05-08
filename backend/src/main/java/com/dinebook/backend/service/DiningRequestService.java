package com.dinebook.backend.service;

import com.dinebook.backend.dto.CreateDiningRequest;
import com.dinebook.backend.dto.DiningRequestDto;
import com.dinebook.backend.model.DiningRequest;
import com.dinebook.backend.model.RequestStatus;
import com.dinebook.backend.model.Restaurant;
import com.dinebook.backend.repository.DiningRequestRepository;
import com.dinebook.backend.service.notification.Notification;
import com.dinebook.backend.service.notification.NotificationFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class DiningRequestService {
    private final DiningRequestRepository diningRequestRepository;
    private final RestaurantService restaurantService;
    private final NotificationFactory notificationFactory;

    public DiningRequestService(
            DiningRequestRepository diningRequestRepository,
            RestaurantService restaurantService,
            NotificationFactory notificationFactory
    ) {
        this.diningRequestRepository = diningRequestRepository;
        this.restaurantService = restaurantService;
        this.notificationFactory = notificationFactory;
    }

    @Transactional
    public DiningRequestDto create(String dinerEmail, CreateDiningRequest request) {
        Restaurant restaurant = restaurantService.findById(request.restaurantId());

        DiningRequest diningRequest = new DiningRequest();
        diningRequest.setRestaurant(restaurant);
        diningRequest.setDinerEmail(dinerEmail);
        diningRequest.setRequestedDateTime(request.requestedDateTime());
        diningRequest.setGuests(request.guests());
        diningRequest.setStatus(RequestStatus.PENDING);

        DiningRequest saved = diningRequestRepository.save(diningRequest);
        sendNotification(
                dinerEmail,
                "Dining request submitted for " + restaurant.getName() + " on " + request.requestedDateTime()
        );
        return toDto(saved);
    }

    public List<DiningRequestDto> myRequests(String dinerEmail) {
        return diningRequestRepository.findByDinerEmailIgnoreCaseOrderByCreatedAtDesc(dinerEmail)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public List<DiningRequestDto> allRequests() {
        return diningRequestRepository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional
    public DiningRequestDto updateStatus(Long requestId, RequestStatus status) {
        if (status == RequestStatus.PENDING) {
            throw new ResponseStatusException(BAD_REQUEST, "Cannot set request back to PENDING");
        }

        DiningRequest diningRequest = diningRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Dining request not found"));

        diningRequest.setStatus(status);
        DiningRequest saved = diningRequestRepository.save(diningRequest);
        sendNotification(
                saved.getDinerEmail(),
                "Your dining request #" + saved.getId() + " is now " + status
        );
        return toDto(saved);
    }

    private void sendNotification(String to, String message) {
        Notification notification = notificationFactory.createNotification("EMAIL");
        notification.send(to, message);
    }

    private DiningRequestDto toDto(DiningRequest entity) {
        return new DiningRequestDto(
                entity.getId(),
                entity.getRestaurant().getId(),
                entity.getRestaurant().getName(),
                entity.getDinerEmail(),
                entity.getRequestedDateTime(),
                entity.getGuests(),
                entity.getStatus(),
                entity.getCreatedAt()
        );
    }
}
