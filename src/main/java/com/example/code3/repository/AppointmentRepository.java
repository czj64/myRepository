package com.example.code3.repository;

import com.example.code3.entity.Appointment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByUserIdOrderByAppointmentDateDesc(Long userId);
    Page<Appointment> findByUserIdOrderByAppointmentDateDesc(Long userId, Pageable pageable);
    Page<Appointment> findByUserIdAndStatusOrderByAppointmentDateDesc(Long userId, String status, Pageable pageable);
    Page<Appointment> findByStatusOrderByAppointmentDateDesc(String status, Pageable pageable);
    List<Appointment> findByDoctorIdAndAppointmentDate(Long doctorId, LocalDate date);
    List<Appointment> findByAppointmentDateOrderByAppointmentTimeAsc(LocalDate date);
    List<Appointment> findByDoctorIdAndStatusOrderByAppointmentDateDesc(Long doctorId, String status);
    long countByStatus(String status);
    long countByAppointmentDate(LocalDate date);
    long countByDoctorIdAndStatus(Long doctorId, String status);
    Page<Appointment> findByDoctorIdOrderByAppointmentDateDesc(Long doctorId, Pageable pageable);
    Page<Appointment> findByDoctorIdAndUserIdOrderByAppointmentDateDesc(Long doctorId, Long userId, Pageable pageable);
    Page<Appointment> findByUserIdAndDoctorNameContaining(Long userId, String doctorName, Pageable pageable);
    Page<Appointment> findByUserIdAndStatusAndDoctorNameContaining(Long userId, String status, String doctorName, Pageable pageable);
    Page<Appointment> findByPatientNameContaining(String patientName, Pageable pageable);
    Page<Appointment> findByStatusAndPatientNameContaining(String status, String patientName, Pageable pageable);
}
