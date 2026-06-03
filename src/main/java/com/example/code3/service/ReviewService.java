package com.example.code3.service;

import com.example.code3.entity.Review;
import java.util.List;

public interface ReviewService {
    Review save(Review review);
    List<Review> findByDoctorId(Long doctorId);
    List<Review> findByUserId(Long userId);
    Review findByAppointmentId(Long appointmentId);
    double getAverageRating(Long doctorId);
}
