package com.dinebook.backend.booking;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiningRequestRepository extends JpaRepository<DiningRequest, Long> {
    List<DiningRequest> findByDinerEmailIgnoreCaseOrderByCreatedAtDesc(String dinerEmail);
}
