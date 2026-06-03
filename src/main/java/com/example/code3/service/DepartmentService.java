package com.example.code3.service;

import com.example.code3.entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DepartmentService {
    List<Department> findAll();
    List<Department> findActiveDepartments();
    Page<Department> findAll(Pageable pageable);
    Department getById(Long id);
    Department save(Department department);
    void deleteById(Long id);
}
