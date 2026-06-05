package com.example.code3.service.impl;

import com.example.code3.entity.Doctor;
import com.example.code3.repository.DoctorRepository;
import com.example.code3.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DoctorServiceImpl implements DoctorService {

    @Autowired
    private DoctorRepository doctorRepository;

    @Override
    public List<Doctor> findByDepartmentId(Long departmentId) {
        return doctorRepository.findByDepartmentId(departmentId);
    }

    @Override
    public List<Doctor> findActiveByDepartmentId(Long departmentId) {
        return doctorRepository.findByDepartmentIdAndActiveTrue(departmentId);
    }

    @Override
    public List<Doctor> findAll() {
        return doctorRepository.findByActiveTrue();
    }

    @Override
    public Page<Doctor> findAll(Pageable pageable) {
        return doctorRepository.findAll(pageable);
    }

    @Override
    public Doctor getById(Long id) {
        return doctorRepository.findById(id).orElse(null);
    }

    @Override
    public Doctor save(Doctor doctor) {
        return doctorRepository.save(doctor);
    }

    @Override
    public void deleteById(Long id) {
        doctorRepository.deleteById(id);
    }

    @Override
    public Doctor findByName(String name) {
        return doctorRepository.findFirstByName(name).orElse(null);
    }

    @Override
    public List<Doctor> findActiveDoctors() {
        return doctorRepository.findByActiveTrue();
    }
}
