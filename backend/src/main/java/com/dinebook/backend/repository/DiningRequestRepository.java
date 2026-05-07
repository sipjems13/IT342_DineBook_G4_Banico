package com.dinebook.backend.repository;

import com.dinebook.backend.model.DiningRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiningRequestRepository extends JpaRepository<DiningRequest, Long> {
    List<DiningRequest> findByDinerEmailIgnoreCaseOrderByCreatedAtDesc(String dinerEmail);
}
