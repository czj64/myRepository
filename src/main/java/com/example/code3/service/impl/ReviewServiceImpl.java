package com.example.code3.service.impl;

import com.example.code3.entity.Review;
import com.example.code3.repository.ReviewRepository;
import com.example.code3.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

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
    
    @Override
    public double getOverallAverageRating() {
        List<Review> all = reviewRepository.findAll();
        if (all.isEmpty()) return 0.0;
        return all.stream().mapToInt(Review::getRating).average().orElse(0.0);
    }
    
    @Override
    public Page<Review> findAllReviews(Pageable pageable) {
        return reviewRepository.findAllByOrderByCreatedAtDesc(pageable);
    }
    
    @Override
    public Page<Review> findReviewsByDoctorId(Long doctorId, Pageable pageable) {
        return reviewRepository.findByDoctorId(doctorId, pageable);
    }
    
    @Override
    public long count() {
        return reviewRepository.count();
    }
    
    @Override
    public long countByRating(Integer rating) {
        return reviewRepository.countByRating(rating);
    }
}