package com.example.code3.controller;

import com.example.code3.entity.Appointment;
import com.example.code3.entity.Department;
import com.example.code3.entity.Doctor;
import com.example.code3.entity.User;
import com.example.code3.service.AppointmentService;
import com.example.code3.service.DepartmentService;
import com.example.code3.service.DoctorService;
import com.example.code3.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private UserService userService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @GetMapping("/")
    public String index(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            model.addAttribute("user", user);
        }
        return "index";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                          @RequestParam String password,
                          @RequestParam String confirmPassword,
                          @RequestParam String name,
                          @RequestParam String phone,
                          Model model) {
        
        // 密码长度校验
        if (password.length() < 6 || password.length() > 20) {
            model.addAttribute("error", "密码长度应为6-20位");
            return "register";
        }

        // 检查两次密码是否一致
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "两次输入的密码不一致");
            return "register";
        }

        // 手机号格式校验
        if (phone != null && !phone.isEmpty() && !phone.matches("^1[3-9]\\d{9}$")) {
            model.addAttribute("error", "请输入有效的11位手机号");
            return "register";
        }

        // 检查用户名是否已存在
        if (userService.existsByUsername(username)) {
            model.addAttribute("error", "该用户名已被使用");
            return "register";
        }

        // 创建用户，编码密码后保存
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setName(name);
        user.setPhone(phone);
        user.setRole(0);

        userService.save(user);

        model.addAttribute("success", "注册成功！请前往登录页面登录");
        return "register";
    }

    @GetMapping("/admin-login")
    public String adminLoginPage() {
        return "admin-login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, 
                       @RequestParam String password, 
                       HttpSession session,
                       Model model) {
        User user = userService.login(username, password);
        if (user != null) {
            if (user.getRole() != 0) {
                model.addAttribute("error", "请使用对应入口登录");
                return "login";
            }
            session.setAttribute("user", user);
            return "redirect:/";
        }
        model.addAttribute("error", "用户名或密码错误");
        return "login";
    }

    @GetMapping("/doctor-login")
    public String doctorLoginPage() {
        return "doctor-login";
    }

    @PostMapping("/doctor/login")
    public String doctorLogin(@RequestParam String username,
                              @RequestParam String password,
                              HttpSession session,
                              Model model) {
        User user = userService.login(username, password);
        if (user != null) {
            if (user.getRole() != 2) {
                model.addAttribute("error", "请使用医生入口登录");
                return "doctor-login";
            }
            Doctor doctor = doctorService.findByName(user.getName());
            if (doctor == null) {
                model.addAttribute("error", "您的医生信息不存在，请联系管理员重新添加账号");
                return "doctor-login";
            }
            session.setAttribute("user", user);
            return "redirect:/doctor/dashboard";
        }
        model.addAttribute("error", "医生账号或密码错误");
        return "doctor-login";
    }

    @PostMapping("/admin/login")
    public String adminLogin(@RequestParam String username, 
                            @RequestParam String password, 
                            HttpSession session,
                            Model model) {
        User user = userService.login(username, password);
        if (user != null) {
            if (user.getRole() != 1) {
                model.addAttribute("error", "请使用对应入口登录");
                return "admin-login";
            }
            session.setAttribute("user", user);
            return "redirect:/admin/dashboard";
        }
        model.addAttribute("error", "管理员账号或密码错误");
        return "admin-login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        User user = (User) session.getAttribute("user");
        Integer role = (user != null) ? user.getRole() : null;
        session.invalidate();
        if (role != null && role == 2) {
            return "redirect:/doctor-login";
        } else if (role != null && role == 1) {
            return "redirect:/admin-login";
        }
        return "redirect:/";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != 1) {
            return "redirect:/admin-login";
        }

        LocalDate today = LocalDate.now();
        List<Department> departments = departmentService.findAll();
        List<Appointment> todayAppointments = appointmentService.findByDate(today);

        model.addAttribute("doctorCount", doctorService.findAll().size());
        model.addAttribute("departmentCount", departments.size());
        model.addAttribute("todayAppointmentCount", todayAppointments.size());
        model.addAttribute("patientCount", userService.countByRole(0));

        model.addAttribute("pendingCount", appointmentService.countByStatus("待就诊"));
        model.addAttribute("confirmedCount", appointmentService.countByStatus("已确认"));
        model.addAttribute("completedCount", appointmentService.countByStatus("已完成"));
        model.addAttribute("cancelledCount", appointmentService.countByStatus("已取消"));
        model.addAttribute("appointmentCount", appointmentService.findAll().size());

        model.addAttribute("todayPending", appointmentService.countTodayByStatus("待就诊"));
        model.addAttribute("todayConfirmed", appointmentService.countTodayByStatus("已确认"));
        model.addAttribute("todayCompleted", appointmentService.countTodayByStatus("已完成"));
        model.addAttribute("todayCancelled", appointmentService.countTodayByStatus("已取消"));
        model.addAttribute("todayAppointments", todayAppointments);
        model.addAttribute("user", user);
        return "admin-dashboard";
    }
}
