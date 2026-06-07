package com.example.code3.repository;

import com.example.code3.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByDoctorIdOrderByCreatedAtDesc(Long doctorId);
    List<Review> findByUserIdOrderByCreatedAtDesc(Long userId);
    Review findByAppointmentId(Long appointmentId);
    Page<Review> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<Review> findByDoctorId(Long doctorId, Pageable pageable);
    long countByRating(Integer rating);
}
