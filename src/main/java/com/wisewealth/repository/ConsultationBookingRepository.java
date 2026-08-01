package com.wisewealth.repository;

import com.wisewealth.entity.ConsultationBooking;
import com.wisewealth.entity.StatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConsultationBookingRepository extends JpaRepository<ConsultationBooking, Long> {
    Page<ConsultationBooking> findByUserUserId(Long userId, Pageable pageable);
    Page<ConsultationBooking> findByUserUserIdAndStatus(Long userId, StatusEnum status, Pageable pageable);
    Optional<ConsultationBooking> findByConsultationIdAndUserUserId(Long consultationId, Long userId);
    Page<ConsultationBooking> findByStatus(StatusEnum status, Pageable pageable);
    Page<ConsultationBooking> findAll(Pageable pageable);
}
