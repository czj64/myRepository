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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员控制器 - 处理所有管理员后台功能
 */
@Controller
public class AdminController {
    
    @Autowired
    private DepartmentService departmentService;
    
    @Autowired
    private DoctorService doctorService;
    
    @Autowired
    private AppointmentService appointmentService;
    
    @Autowired
    private UserService userService;

    @Autowired
    private ReviewService reviewService;
    
    /**
     * 检查是否是管理员
     */
    private boolean isAdmin(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != 1) {
            return false;
        }
        model.addAttribute("user", user);
        return true;
    }
    
    // ==================== 科室管理 ====================
    
    @GetMapping("/admin/departments")
    public String departmentList(@RequestParam(defaultValue = "0") int page,
                                  HttpSession session, Model model) {
        if (!isAdmin(session, model)) return "redirect:/admin-login";
        
        Pageable pageable = PageRequest.of(page, 10);
        Page<Department> deptPage = departmentService.findAll(pageable);
        
        model.addAttribute("departments", deptPage.getContent());
        model.addAttribute("deptPage", deptPage);
        return "admin-departments";
    }
    
    @GetMapping("/admin/department/add")
    public String addDepartmentPage(HttpSession session, Model model) {
        if (!isAdmin(session, model)) return "redirect:/admin-login";
        return "admin-department-form";
    }
    
    @PostMapping("/admin/department/save")
    public String saveDepartment(@RequestParam String name,
                                 @RequestParam(required = false) String description,
                                 HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != 1) return "redirect:/admin-login";
        
        Department dept = new Department();
        dept.setName(name);
        dept.setDescription(description);
        departmentService.save(dept);
        return "redirect:/admin/departments";
    }
    
    @GetMapping("/admin/department/offline")
    public String offlineDepartment(@RequestParam Long id, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != 1) return "redirect:/admin-login";
        
        Department dept = departmentService.getById(id);
        if (dept != null) {
            dept.setActive(false);
            departmentService.save(dept);
            for (Doctor d : doctorService.findByDepartmentId(id)) {
                d.setActive(false);
                doctorService.save(d);
            }
        }
        return "redirect:/admin/departments";
    }
    
    @GetMapping("/admin/department/enable")
    public String enableDepartment(@RequestParam Long id, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != 1) return "redirect:/admin-login";
        
        Department dept = departmentService.getById(id);
        if (dept != null) {
            dept.setActive(true);
            departmentService.save(dept);
        }
        return "redirect:/admin/departments";
    }
    
    @GetMapping("/admin/department/edit")
    public String editDepartmentPage(@RequestParam Long id, HttpSession session, Model model) {
        if (!isAdmin(session, model)) return "redirect:/admin-login";
        
        Department dept = departmentService.getById(id);
        model.addAttribute("department", dept);
        return "admin-department-form";
    }
    
    @PostMapping("/admin/department/update")
    public String updateDepartment(@RequestParam Long id,
                                   @RequestParam String name,
                                   @RequestParam(required = false) String description,
                                   HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != 1) return "redirect:/admin-login";
        
        Department dept = departmentService.getById(id);
        if (dept != null) {
            dept.setName(name);
            dept.setDescription(description);
            departmentService.save(dept);
        }
        return "redirect:/admin/departments";
    }
    
    // ==================== 医生管理 ====================
    
    @GetMapping("/admin/doctors")
    public String doctorList(@RequestParam(defaultValue = "0") int page,
                             HttpSession session, Model model) {
        if (!isAdmin(session, model)) return "redirect:/admin-login";
        
        Pageable pageable = PageRequest.of(page, 10);
        Page<Doctor> doctorPage = doctorService.findAll(pageable);
        List<Department> departments = departmentService.findAll();
        
        model.addAttribute("doctors", doctorPage.getContent());
        model.addAttribute("doctorPage", doctorPage);
        model.addAttribute("departments", departments);
        return "admin-doctors";
    }
    
    @GetMapping("/admin/doctor/add")
    public String addDoctorPage(HttpSession session, Model model) {
        if (!isAdmin(session, model)) return "redirect:/admin-login";
        
        List<Department> departments = departmentService.findAll();
        model.addAttribute("departments", departments);
        return "admin-doctor-form";
    }
    
    @PostMapping("/admin/doctor/save")
    public String saveDoctor(@RequestParam String name,
                            @RequestParam String title,
                            @RequestParam Long departmentId,
                            @RequestParam(required = false) String description,
                            @RequestParam String availableTime,
                            @RequestParam String username,
                            @RequestParam String password,
                            HttpSession session,
                            Model model) {
        User admin = (User) session.getAttribute("user");
        if (admin == null || admin.getRole() != 1) return "redirect:/admin-login";
        
        if (!isValidTimeFormat(availableTime)) {
            model.addAttribute("departments", departmentService.findAll());
            model.addAttribute("error", "坐诊时间格式不正确，请输入具体时间，如：周一至周五 09:00-12:00");
            return "admin-doctor-form";
        }
        
        if (userService.existsByUsername(username)) {
            model.addAttribute("departments", departmentService.findAll());
            model.addAttribute("error", "该登录账号已被使用，请换一个");
            return "admin-doctor-form";
        }

        Doctor doctor = new Doctor();
        doctor.setName(name);
        doctor.setTitle(title);
        doctor.setDepartment(departmentService.getById(departmentId));
        doctor.setDescription(description);
        doctor.setAvailableTime(availableTime);
        doctorService.save(doctor);

        User doctorUser = new User();
        doctorUser.setUsername(username);
        doctorUser.setPassword(password);
        doctorUser.setName(name);
        doctorUser.setPhone(null);
        doctorUser.setRole(2);
        userService.save(doctorUser);

        return "redirect:/admin/doctors";
    }
    
    private boolean isValidTimeFormat(String availableTime) {
        if (availableTime == null || availableTime.trim().isEmpty()) {
            return false;
        }
        
        java.util.regex.Pattern timePattern = java.util.regex.Pattern.compile("\\d{1,2}:\\d{2}\\s*-\\s*\\d{1,2}:\\d{2}");
        java.util.regex.Matcher matcher = timePattern.matcher(availableTime);
        return matcher.find();
    }
    
    @GetMapping("/admin/doctor/offline")
    public String offlineDoctor(@RequestParam Long id, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != 1) return "redirect:/admin-login";
        
        Doctor doctor = doctorService.getById(id);
        if (doctor != null) {
            doctor.setActive(false);
            doctorService.save(doctor);
        }
        return "redirect:/admin/doctors";
    }
    
    @GetMapping("/admin/doctor/enable")
    public String enableDoctor(@RequestParam Long id, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != 1) return "redirect:/admin-login";
        
        Doctor doctor = doctorService.getById(id);
        if (doctor != null) {
            doctor.setActive(true);
            doctorService.save(doctor);
        }
        return "redirect:/admin/doctors";
    }
    
    // ==================== 预约管理 ====================
    
    @GetMapping("/admin/appointments")
    public String appointmentList(@RequestParam(defaultValue = "0") int page,
                                  @RequestParam(required = false) String status,
                                  @RequestParam(required = false) String keyword,
                                  HttpSession session, Model model) {
        if (!isAdmin(session, model)) return "redirect:/admin-login";
        
        Pageable pageable = PageRequest.of(page, 15);
        Page<?> appointmentPage;
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.trim();
            if (status != null && !status.isEmpty()) {
                appointmentPage = appointmentService.searchByStatusAndPatientName(status, kw, pageable);
            } else {
                appointmentPage = appointmentService.searchByPatientName(kw, pageable);
            }
        } else if (status != null && !status.isEmpty()) {
            appointmentPage = appointmentService.findByStatus(status, pageable);
        } else {
            appointmentPage = appointmentService.findAll(pageable);
        }
        
        model.addAttribute("appointments", appointmentPage.getContent());
        model.addAttribute("appointmentPage", appointmentPage);
        model.addAttribute("currentStatus", status);
        model.addAttribute("keyword", keyword);
        return "admin-appointments";
    }
    
    @PostMapping("/admin/appointment/updateStatus")
    public String updateAppointmentStatus(@RequestParam Long id,
                                         @RequestParam String status,
                                         HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != 1) return "redirect:/admin-login";
        
        var appointment = appointmentService.getById(id);
        if (appointment != null) {
            appointment.setStatus(status);
            appointmentService.save(appointment);
        }
        return "redirect:/admin/appointments";
    }
    
    // ==================== 用户管理 ====================
    
    @GetMapping("/admin/users")
    public String userList(@RequestParam(defaultValue = "0") int page,
                          @RequestParam(required = false) String keyword,
                          HttpSession session, Model model) {
        if (!isAdmin(session, model)) return "redirect:/admin-login";
        
        Pageable pageable = PageRequest.of(page, 15);
        Page<User> userPage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            userPage = userService.searchUsers(keyword.trim(), pageable);
        } else {
            userPage = userService.findAll(pageable);
        }
        
        model.addAttribute("users", userPage.getContent());
        model.addAttribute("userPage", userPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("patientCount", userService.countByRole(0));
        model.addAttribute("adminCount", userService.countByRole(1));
        model.addAttribute("doctorCount", userService.countByRole(2));
        return "admin-users";
    }
    
    // ==================== 数据统计 ====================
    
    @GetMapping("/admin/statistics")
    public String statistics(HttpSession session, Model model) {
        if (!isAdmin(session, model)) return "redirect:/admin-login";
        
        // 评价统计
        long totalReviews = reviewService.count();
        double avgRating = reviewService.getOverallAverageRating();
        long fiveStarCount = reviewService.countByRating(5);
        long fourStarCount = reviewService.countByRating(4);
        long threeStarCount = reviewService.countByRating(3);
        long twoStarCount = reviewService.countByRating(2);
        long oneStarCount = reviewService.countByRating(1);
        long positiveCount = fiveStarCount + fourStarCount; // 4-5星好评
        double positiveRate = totalReviews > 0 ? (double) positiveCount / totalReviews * 100 : 0.0;
        
        model.addAttribute("totalReviews", totalReviews);
        model.addAttribute("avgRating", avgRating);
        model.addAttribute("fiveStarCount", fiveStarCount);
        model.addAttribute("fourStarCount", fourStarCount);
        model.addAttribute("threeStarCount", threeStarCount);
        model.addAttribute("twoStarCount", twoStarCount);
        model.addAttribute("oneStarCount", oneStarCount);
        model.addAttribute("positiveRate", positiveRate);
        
        // 各科室预约量统计（已完成+待就诊）
        List<Department> departments = departmentService.findAll();
        List<Appointment> allAppts = appointmentService.findAll();
        Map<Long, Long> deptApptCount = new HashMap<>();
        Map<String, Long> deptNameCount = new LinkedHashMap<>();
        
        for (Department dept : departments) {
            long count = allAppts.stream()
                .filter(a -> a.getDoctor() != null && a.getDoctor().getDepartment() != null
                    && a.getDoctor().getDepartment().getId().equals(dept.getId()))
                .count();
            if (count > 0) {
                deptNameCount.put(dept.getName(), count);
            }
        }
        
        // 医生评分排行（取前10）
        List<Doctor> allDoctors = doctorService.findAll();
        List<Map<String, Object>> doctorRatingList = new ArrayList<>();
        for (Doctor doc : allDoctors) {
            double dAvg = reviewService.getAverageRating(doc.getId());
            if (dAvg > 0) {
                Map<String, Object> m = new HashMap<>();
                m.put("name", doc.getName());
                m.put("rating", dAvg);
                m.put("dept", doc.getDepartment() != null ? doc.getDepartment().getName() : "");
                doctorRatingList.add(m);
            }
        }
        doctorRatingList.sort((a, b) -> Double.compare((double) b.get("rating"), (double) a.get("rating")));
        if (doctorRatingList.size() > 10) {
            doctorRatingList = doctorRatingList.subList(0, 10);
        }
        
        model.addAttribute("deptApptNames", deptNameCount.keySet());
        model.addAttribute("deptApptCounts", deptNameCount.values());
        model.addAttribute("doctorRatingList", doctorRatingList);
        
        return "admin-statistics";
    }

    // ==================== 评价管理 ====================

    @GetMapping("/admin/reviews")
    public String reviewList(@RequestParam(defaultValue = "0") int page,
                             @RequestParam(required = false) Long doctorId,
                             HttpSession session, Model model) {
        if (!isAdmin(session, model)) return "redirect:/admin-login";

        Pageable pageable = PageRequest.of(page, 15);
        Page<Review> reviewPage;
        if (doctorId != null) {
            reviewPage = reviewService.findReviewsByDoctorId(doctorId, pageable);
            Doctor doctor = doctorService.getById(doctorId);
            model.addAttribute("filterDoctor", doctor);
        } else {
            reviewPage = reviewService.findAllReviews(pageable);
        }

        List<Doctor> allDoctors = doctorService.findAll();

        model.addAttribute("reviews", reviewPage.getContent());
        model.addAttribute("reviewPage", reviewPage);
        model.addAttribute("allDoctors", allDoctors);
        return "admin-reviews";
    }
}
