package com.example.code3.controller;

import com.example.code3.entity.Appointment;
import com.example.code3.entity.Department;
import com.example.code3.entity.Doctor;
import com.example.code3.entity.Review;
import com.example.code3.entity.User;
import com.example.code3.service.AppointmentService;
import com.example.code3.service.DepartmentService;
import com.example.code3.service.DoctorService;
import com.example.code3.service.ReviewService;
import com.example.code3.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private UserService userService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private ReviewService reviewService;

    private Doctor getCurrentDoctor(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != 2) {
            return null;
        }
        return doctorService.findByName(user.getName());
    }

    private boolean isDoctor(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != 2) {
            return false;
        }
        Doctor doctor = doctorService.findByName(user.getName());
        model.addAttribute("user", user);
        model.addAttribute("doctor", doctor);
        return true;
    }

    // ==================== 患者端-医生浏览 ====================

    @GetMapping("/doctors")
    public String doctors(@RequestParam Long departmentId, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        List<Doctor> doctors = doctorService.findActiveByDepartmentId(departmentId);
        Department department = departmentService.getById(departmentId);
        model.addAttribute("doctors", doctors);
        model.addAttribute("department", department);
        model.addAttribute("user", user);
        return "doctors";
    }

    @GetMapping("/doctor/detail")
    public String doctorDetail(@RequestParam Long id, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        Doctor doctor = doctorService.getById(id);
        List<Review> reviews = reviewService.findByDoctorId(id);
        double avgRating = reviewService.getAverageRating(id);
        model.addAttribute("doctor", doctor);
        model.addAttribute("reviews", reviews);
        model.addAttribute("avgRating", avgRating);
        model.addAttribute("user", user);
        return "doctor-detail";
    }

    // ==================== 医生端-专属功能 ====================

    @GetMapping("/doctor/dashboard")
    public String dashboard(HttpSession session, Model model) {
        if (!isDoctor(session, model)) return "redirect:/doctor-login";
        Doctor doctor = getCurrentDoctor(session);
        if (doctor == null) return "redirect:/doctor-login";

        LocalDate today = LocalDate.now();
        List<Appointment> todayAppointments = appointmentService.findByDoctorIdAndDate(doctor.getId(), today);
        long pendingCount = todayAppointments.stream().filter(a -> "待就诊".equals(a.getStatus())).count();
        long completedCount = appointmentService.countByDoctorAndStatus(doctor.getId(), "已完成");
        long totalPending = appointmentService.countByDoctorAndStatus(doctor.getId(), "待就诊");

        model.addAttribute("todayAppointments", todayAppointments);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("totalPending", totalPending);
        model.addAttribute("todayDate", today);
        model.addAttribute("doctorId", doctor.getId());
        return "doctor-dashboard";
    }

    @GetMapping("/doctor/patients")
    public String patients(@RequestParam(defaultValue = "pending") String tab,
                           HttpSession session, Model model) {
        if (!isDoctor(session, model)) return "redirect:/doctor-login";
        Doctor doctor = getCurrentDoctor(session);
        if (doctor == null) return "redirect:/doctor-login";

        List<Appointment> pendingPatients = appointmentService.findByDoctorIdAndStatus(doctor.getId(), "待就诊");
        List<Appointment> completedPatients = appointmentService.findByDoctorIdAndStatus(doctor.getId(), "已完成");
        List<Appointment> cancelledPatients = appointmentService.findByDoctorIdAndStatus(doctor.getId(), "已取消");

        model.addAttribute("pendingPatients", pendingPatients);
        model.addAttribute("completedPatients", completedPatients);
        model.addAttribute("cancelledPatients", cancelledPatients);
        model.addAttribute("tab", tab);
        return "doctor-patients";
    }

    @GetMapping("/doctor/records")
    public String records(@RequestParam(required = false) Long patientId,
                          @RequestParam(defaultValue = "0") int page,
                          HttpSession session, Model model) {
        if (!isDoctor(session, model)) return "redirect:/doctor-login";
        Doctor doctor = getCurrentDoctor(session);
        if (doctor == null) return "redirect:/doctor-login";

        Pageable pageable = PageRequest.of(page, 10);
        Page<Appointment> recordsPage;
        if (patientId != null) {
            recordsPage = appointmentService.findByDoctorIdAndUserId(doctor.getId(), patientId, pageable);
        } else {
            recordsPage = appointmentService.findByDoctorId(doctor.getId(), pageable);
        }

        model.addAttribute("records", recordsPage.getContent());
        model.addAttribute("recordsPage", recordsPage);
        model.addAttribute("patientId", patientId);
        return "doctor-records";
    }

    @GetMapping("/doctor/record/edit")
    public String editRecord(@RequestParam Long id, HttpSession session, Model model) {
        if (!isDoctor(session, model)) return "redirect:/doctor-login";
        Doctor doctor = getCurrentDoctor(session);
        if (doctor == null) return "redirect:/doctor-login";

        Appointment appointment = appointmentService.getById(id);
        if (appointment == null || !appointment.getDoctor().getId().equals(doctor.getId())) {
            return "redirect:/doctor/records";
        }
        model.addAttribute("record", appointment);
        model.addAttribute("cancelled", "已取消".equals(appointment.getStatus()));
        model.addAttribute("completed", "已完成".equals(appointment.getStatus()));
        return "doctor-record-edit";
    }

    @PostMapping("/doctor/record/save")
    public String saveRecord(@RequestParam Long id,
                             @RequestParam String diagnosis,
                             @RequestParam String prescription,
                             @RequestParam(required = false) String note,
                             HttpSession session) {
        Doctor doctor = getCurrentDoctor(session);
        if (doctor == null) return "redirect:/doctor-login";

        Appointment appointment = appointmentService.getById(id);
        if (appointment != null && appointment.getDoctor().getId().equals(doctor.getId())) {
            if ("已取消".equals(appointment.getStatus()) || "已完成".equals(appointment.getStatus())) {
                return "redirect:/doctor/records";
            }
            appointment.setPatientName(appointment.getPatientName() + "|诊:" + diagnosis + "|方:" + prescription);
            if (note != null && !note.isEmpty()) {
                appointment.setPatientName(appointment.getPatientName() + "|注:" + note);
            }
            appointment.setStatus("已完成");
            appointmentService.save(appointment);
        }
        return "redirect:/doctor/records";
    }

    @GetMapping("/doctor/schedule")
    public String schedule(HttpSession session, Model model) {
        if (!isDoctor(session, model)) return "redirect:/doctor-login";
        Doctor doctor = getCurrentDoctor(session);
        if (doctor == null) return "redirect:/doctor-login";

        model.addAttribute("availableTime", doctor.getAvailableTime());
        return "doctor-schedule";
    }

    @GetMapping("/doctor/messages")
    public String messages(HttpSession session, Model model) {
        if (!isDoctor(session, model)) return "redirect:/doctor-login";
        Doctor doctor = getCurrentDoctor(session);
        if (doctor == null) return "redirect:/doctor-login";

        LocalDate today = LocalDate.now();
        List<Appointment> todayAppointments = appointmentService.findByDoctorIdAndDate(doctor.getId(), today);
        long newPatientCount = todayAppointments.stream().filter(a -> "待就诊".equals(a.getStatus())).count();

        model.addAttribute("todayAppointments", todayAppointments);
        model.addAttribute("newPatientCount", newPatientCount);
        return "doctor-messages";
    }

    @GetMapping("/doctor/profile")
    public String profile(HttpSession session, Model model) {
        if (!isDoctor(session, model)) return "redirect:/doctor-login";
        Doctor doctor = getCurrentDoctor(session);
        if (doctor == null) return "redirect:/doctor-login";

        model.addAttribute("doctorInfo", doctor);
        return "doctor-profile";
    }

    @PostMapping("/doctor/profile/update")
    public String updateProfile(@RequestParam(required = false) String phone,
                                @RequestParam(required = false) String oldPassword,
                                @RequestParam(required = false) String newPassword,
                                HttpSession session,
                                Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != 2) return "redirect:/doctor-login";
        Doctor doctor = getCurrentDoctor(session);
        if (doctor == null) return "redirect:/doctor-login";

        if (phone != null && !phone.isEmpty()) {
            user.setPhone(phone);
        }

        if (oldPassword != null && !oldPassword.isEmpty()
            && newPassword != null && !newPassword.isEmpty()) {
            if (!user.getPassword().equals(oldPassword)) {
                model.addAttribute("user", user);
                model.addAttribute("doctor", doctor);
                model.addAttribute("doctorInfo", doctor);
                model.addAttribute("error", "原密码错误");
                return "doctor-profile";
            }
            user.setPassword(newPassword);
        }

        userService.save(user);
        session.setAttribute("user", user);

        model.addAttribute("user", user);
        model.addAttribute("doctor", doctor);
        model.addAttribute("doctorInfo", doctor);
        model.addAttribute("success", "个人信息更新成功");
        return "doctor-profile";
    }
}