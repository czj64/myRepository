package com.example.code3.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "doctor")
@Data
public class Doctor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String title;
    
    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;
    
    private String description;
    
    @Column(nullable = false)
    private String availableTime;

    @Column(nullable = false)
    private Boolean active = true;
}
