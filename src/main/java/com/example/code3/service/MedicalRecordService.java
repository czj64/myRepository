package com.example.code3.service;

import com.example.code3.entity.MedicalRecord;

import java.util.List;

public interface MedicalRecordService {
    MedicalRecord save(MedicalRecord medicalRecord);
    MedicalRecord findByAppointmentId(Long appointmentId);
    List<MedicalRecord> findByUserId(Long userId);
}
