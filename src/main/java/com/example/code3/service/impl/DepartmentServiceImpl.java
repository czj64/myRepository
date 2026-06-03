package com.example.code3.service.impl;

import com.example.code3.entity.Department;
import com.example.code3.repository.DepartmentRepository;
import com.example.code3.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartmentServiceImpl implements DepartmentService {
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Override
    public List<Department> findAll() {
        return departmentRepository.findAll();
    }
    
    @Override
    public Page<Department> findAll(Pageable pageable) {
        return departmentRepository.findAll(pageable);
    }
    
    @Override
    public Department getById(Long id) {
        return departmentRepository.findById(id).orElse(null);
    }
    
    @Override
    public Department save(Department department) {
        return departmentRepository.save(department);
    }
    
    @Override
    public void deleteById(Long id) {
        departmentRepository.deleteById(id);
    }
    
    @Override
    public List<Department> findActiveDepartments() {
        return departmentRepository.findByActiveTrue();
    }
}
