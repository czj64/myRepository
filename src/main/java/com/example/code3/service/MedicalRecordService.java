package com.example.code3.service;

import com.example.code3.entity.MedicalRecord;

public interface MedicalRecordService {
    MedicalRecord save(MedicalRecord medicalRecord);
    MedicalRecord findByAppointmentId(Long appointmentId);
}
