package com.example.code3.repository;

import com.example.code3.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Optional<Department> findByName(String name);
    List<Department> findByActiveTrue();
    List<Department> findAllByOrderByNameAsc();
}
