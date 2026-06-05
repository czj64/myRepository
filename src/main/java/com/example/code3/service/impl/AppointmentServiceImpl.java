package com.example.code3.service.impl;

import com.example.code3.entity.Appointment;
import com.example.code3.repository.AppointmentRepository;
import com.example.code3.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class AppointmentServiceImpl implements AppointmentService {
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @Override
    public Appointment save(Appointment appointment) {
        return appointmentRepository.save(appointment);
    }
    
    @Override
    public List<Appointment> findByUserId(Long userId) {
        return appointmentRepository.findByUserIdOrderByAppointmentDateDesc(userId);
    }
    
    @Override
    public Page<Appointment> findByUserId(Long userId, Pageable pageable) {
        return appointmentRepository.findByUserIdOrderByAppointmentDateDesc(userId, pageable);
    }

    @Override
    public Page<Appointment> findByUserIdAndStatus(Long userId, String status, Pageable pageable) {
        return appointmentRepository.findByUserIdAndStatusOrderByAppointmentDateDesc(userId, status, pageable);
    }

    @Override
    public Page<Appointment> findAll(Pageable pageable) {
        return appointmentRepository.findAll(pageable);
    }
    
    @Override
    public Page<Appointment> findByStatus(String status, Pageable pageable) {
        return appointmentRepository.findByStatusOrderByAppointmentDateDesc(status, pageable);
    }
    
    @Override
    public Appointment getById(Long id) {
        return appointmentRepository.findById(id).orElse(null);
    }
    
    @Override
    public void deleteById(Long id) {
        appointmentRepository.deleteById(id);
    }
    
    @Override
    public List<Appointment> findAll() {
        return appointmentRepository.findAll();
    }
    
    @Override
    public boolean hasTimeConflict(Long doctorId, LocalDate date, LocalTime time, Long excludeAppointmentId) {
        List<Appointment> appointments = appointmentRepository.findByDoctorIdAndAppointmentDate(doctorId, date);
        for (Appointment appointment : appointments) {
            if (excludeAppointmentId != null && appointment.getId().equals(excludeAppointmentId)) {
                continue;
            }
            // 检查时间是否冲突（前后30分钟内）
            int diffMinutes = Math.abs(appointment.getAppointmentTime().getHour() * 60 + appointment.getAppointmentTime().getMinute() 
                                     - (time.getHour() * 60 + time.getMinute()));
            if (diffMinutes < 60) { // 至少间隔1小时
                return true;
            }
        }
        return false;
    }
    
    @Override
    public long countByStatus(String status) {
        return appointmentRepository.countByStatus(status);
    }
    
    @Override
    public long countToday() {
        return appointmentRepository.countByAppointmentDate(LocalDate.now());
    }

    @Override
    public long countTodayByStatus(String status) {
        return appointmentRepository.countByAppointmentDateAndStatus(LocalDate.now(), status);
    }

    @Override
    public List<Appointment> findByDate(LocalDate date) {
        return appointmentRepository.findByAppointmentDateOrderByAppointmentTimeAsc(date);
    }

    @Override
    public List<Appointment> findByDoctorIdAndDate(Long doctorId, LocalDate date) {
        return appointmentRepository.findByDoctorIdAndAppointmentDate(doctorId, date);
    }

    @Override
    public List<Appointment> findByDoctorIdAndStatus(Long doctorId, String status) {
        return appointmentRepository.findByDoctorIdAndStatusOrderByAppointmentDateDesc(doctorId, status);
    }

    @Override
    public long countByDoctorAndStatus(Long doctorId, String status) {
        return appointmentRepository.countByDoctorIdAndStatus(doctorId, status);
    }

    @Override
    public Page<Appointment> findByDoctorId(Long doctorId, Pageable pageable) {
        return appointmentRepository.findByDoctorIdOrderByAppointmentDateDesc(doctorId, pageable);
    }

    @Override
    public Page<Appointment> findByDoctorIdAndUserId(Long doctorId, Long userId, Pageable pageable) {
        return appointmentRepository.findByDoctorIdAndUserIdOrderByAppointmentDateDesc(doctorId, userId, pageable);
    }

    @Override
    public Page<Appointment> searchByUserIdAndDoctorName(Long userId, String doctorName, Pageable pageable) {
        return appointmentRepository.findByUserIdAndDoctorNameContaining(userId, doctorName, pageable);
    }

    @Override
    public Page<Appointment> searchByUserIdAndStatusAndDoctorName(Long userId, String status, String doctorName, Pageable pageable) {
        return appointmentRepository.findByUserIdAndStatusAndDoctorNameContaining(userId, status, doctorName, pageable);
    }

    @Override
    public Page<Appointment> searchByPatientName(String patientName, Pageable pageable) {
        return appointmentRepository.findByPatientNameContaining(patientName, pageable);
    }

    @Override
    public Page<Appointment> searchByStatusAndPatientName(String status, String patientName, Pageable pageable) {
        return appointmentRepository.findByStatusAndPatientNameContaining(status, patientName, pageable);
    }
}
