package com.example.code3.service;

import com.example.code3.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ReviewService {
    Review save(Review review);
    List<Review> findByDoctorId(Long doctorId);
    List<Review> findByUserId(Long userId);
    Review findByAppointmentId(Long appointmentId);
    double getAverageRating(Long doctorId);
    double getOverallAverageRating();
    Page<Review> findAllReviews(Pageable pageable);
    Page<Review> findReviewsByDoctorId(Long doctorId, Pageable pageable);
    long count();
    long countByRating(Integer rating);
}
