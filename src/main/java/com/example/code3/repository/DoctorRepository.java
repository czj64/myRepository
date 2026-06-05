package com.example.code3.repository;

import com.example.code3.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    List<Doctor> findByDepartmentId(Long departmentId);
    List<Doctor> findByDepartmentIdOrderByName(Long departmentId);
    Optional<Doctor> findFirstByName(String name);
    List<Doctor> findByActiveTrue();
    List<Doctor> findByDepartmentIdAndActiveTrue(Long departmentId);
}
