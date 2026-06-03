package com.example.code3.service;

import com.example.code3.entity.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DoctorService {
    List<Doctor> findByDepartmentId(Long departmentId);
    Doctor getById(Long id);
    Doctor save(Doctor doctor);
    void deleteById(Long id);
    Page<Doctor> findAll(Pageable pageable);
    List<Doctor> findAll();
    List<Doctor> findActiveDoctors();
    List<Doctor> findActiveByDepartmentId(Long departmentId);
    Doctor findByName(String name);
}
