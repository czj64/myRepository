package com.example.code3.service.impl;

import com.example.code3.entity.Review;
import com.example.code3.repository.ReviewRepository;
import com.example.code3.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewServiceImpl implements ReviewService {
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    @Override
    public Review save(Review review) {
        return reviewRepository.save(review);
    }
    
    @Override
    public List<Review> findByDoctorId(Long doctorId) {
        return reviewRepository.findByDoctorIdOrderByCreatedAtDesc(doctorId);
    }
    
    @Override
    public List<Review> findByUserId(Long userId) {
        return reviewRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    @Override
    public Review findByAppointmentId(Long appointmentId) {
        return reviewRepository.findByAppointmentId(appointmentId);
    }
    
    @Override
    public double getAverageRating(Long doctorId) {
        List<Review> reviews = findByDoctorId(doctorId);
        if (reviews.isEmpty()) {
            return 0.0;
        }
        return reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }
}
