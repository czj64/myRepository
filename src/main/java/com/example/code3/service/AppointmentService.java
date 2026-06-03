package com.example.code3.service;

import com.example.code3.entity.Appointment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AppointmentService {
    Appointment save(Appointment appointment);
    List<Appointment> findByUserId(Long userId);
    Page<Appointment> findByUserId(Long userId, Pageable pageable);
    Page<Appointment> findByUserIdAndStatus(Long userId, String status, Pageable pageable);
    Page<Appointment> findAll(Pageable pageable);
    Page<Appointment> findByStatus(String status, Pageable pageable);
    Appointment getById(Long id);
    void deleteById(Long id);
    List<Appointment> findAll();
    boolean hasTimeConflict(Long doctorId, LocalDate date, java.time.LocalTime time, Long excludeAppointmentId);
    long countByStatus(String status);
    long countToday();
    List<Appointment> findByDate(LocalDate date);
    List<Appointment> findByDoctorIdAndDate(Long doctorId, LocalDate date);
    List<Appointment> findByDoctorIdAndStatus(Long doctorId, String status);
    long countByDoctorAndStatus(Long doctorId, String status);
    Page<Appointment> findByDoctorId(Long doctorId, Pageable pageable);
    Page<Appointment> findByDoctorIdAndUserId(Long doctorId, Long userId, Pageable pageable);
    Page<Appointment> searchByUserIdAndDoctorName(Long userId, String doctorName, Pageable pageable);
    Page<Appointment> searchByUserIdAndStatusAndDoctorName(Long userId, String status, String doctorName, Pageable pageable);
    Page<Appointment> searchByPatientName(String patientName, Pageable pageable);
    Page<Appointment> searchByStatusAndPatientName(String status, String patientName, Pageable pageable);
}
