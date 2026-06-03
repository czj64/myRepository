package com.example.code3.controller;

import com.example.code3.entity.Appointment;
import com.example.code3.entity.Doctor;
import com.example.code3.entity.User;
import com.example.code3.exception.BusinessException;
import com.example.code3.service.AppointmentService;
import com.example.code3.service.DoctorService;
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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
public class AppointmentController {
    
    @Autowired
    private AppointmentService appointmentService;
    
    @Autowired
    private DoctorService doctorService;
    
    @Autowired
    private UserService userService;

    @GetMapping("/patient/records")
    public String patientRecords(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        List<Appointment> records = appointmentService.findByUserId(user.getId());
        model.addAttribute("records", records);
        model.addAttribute("user", user);
        return "patient-records";
    }
    
    @GetMapping("/appointment/create")
    public String createAppointment(@RequestParam Long doctorId, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        Doctor doctor = doctorService.getById(doctorId);
        model.addAttribute("doctor", doctor);
        model.addAttribute("user", user);
        return "appointment-create";
    }
    
    @PostMapping("/appointment/submit")
    public String submitAppointment(@RequestParam Long doctorId,
                                    @RequestParam String appointmentDate,
                                    @RequestParam String appointmentTime,
                                    @RequestParam String patientName,
                                    @RequestParam String patientPhone,
                                    HttpSession session,
                                    Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        if (patientPhone == null || patientPhone.trim().isEmpty()) {
            Doctor doctor = doctorService.getById(doctorId);
            model.addAttribute("doctor", doctor);
            model.addAttribute("user", user);
            model.addAttribute("error", "联系电话不能为空");
            return "appointment-create";
        }

        Doctor doctor = doctorService.getById(doctorId);
        LocalDate date = LocalDate.parse(appointmentDate);
        LocalTime time = LocalTime.parse(appointmentTime);

        if (date.isBefore(LocalDate.now()) || (date.isEqual(LocalDate.now()) && time.isBefore(LocalTime.now()))) {
            model.addAttribute("doctor", doctor);
            model.addAttribute("user", user);
            model.addAttribute("error", "不能预约过去的时间，请选择未来的日期和时间");
            return "appointment-create";
        }

        if (!isWithinAvailableTime(doctor.getAvailableTime(), date, time)) {
            model.addAttribute("doctor", doctor);
            model.addAttribute("user", user);
            model.addAttribute("error", "预约时间不在医生坐诊时间内，请选择" + doctor.getAvailableTime());
            return "appointment-create";
        }

        if (appointmentService.hasTimeConflict(doctorId, date, time, null)) {
            model.addAttribute("doctor", doctor);
            model.addAttribute("user", user);
            model.addAttribute("error", "该时间段已被预约，请选择其他时间");
            return "appointment-create";
        }

        Appointment appointment = new Appointment();
        appointment.setUser(user);
        appointment.setDoctor(doctor);
        appointment.setAppointmentDate(date);
        appointment.setAppointmentTime(time);
        appointment.setPatientName(patientName);
        appointment.setPatientPhone(patientPhone);
        appointment.setStatus("待就诊");

        appointmentService.save(appointment);

        model.addAttribute("message", "预约成功！");
        model.addAttribute("user", user);
        return "appointment-success";
    }

    private boolean isWithinAvailableTime(String availableTime, LocalDate date, LocalTime time) {
        if (availableTime == null || availableTime.trim().isEmpty()) {
            return false;
        }

        DayOfWeek dayOfWeek = date.getDayOfWeek();
        String dayName = getDayName(dayOfWeek);

        Pattern timePattern = Pattern.compile("(\\d{1,2}:\\d{2})\\s*[-~]\\s*(\\d{1,2}:\\d{2})");
        Matcher timeMatcher = timePattern.matcher(availableTime);

        boolean timeInRange = false;
        while (timeMatcher.find()) {
            LocalTime startTime = LocalTime.parse(timeMatcher.group(1), DateTimeFormatter.ofPattern("H:mm"));
            LocalTime endTime = LocalTime.parse(timeMatcher.group(2), DateTimeFormatter.ofPattern("H:mm"));
            if (!time.isBefore(startTime) && !time.isAfter(endTime)) {
                timeInRange = true;
                break;
            }
        }
        if (!timeInRange) return false;

        String dayPart = availableTime.toLowerCase();
        if (dayPart.contains("每天") || dayPart.contains(" everyday")) {
            return true;
        }

        if ((dayName.equals("周一") || dayName.equals("Monday")) && (dayPart.contains("周一") || dayPart.contains("monday") || dayPart.contains("星期一") || dayPart.contains("mon"))) {
            return true;
        }
        if ((dayName.equals("周二") || dayName.equals("Tuesday")) && (dayPart.contains("周二") || dayPart.contains("tuesday") || dayPart.contains("星期二") || dayPart.contains("tue"))) {
            return true;
        }
        if ((dayName.equals("周三") || dayName.equals("Wednesday")) && (dayPart.contains("周三") || dayPart.contains("wednesday") || dayPart.contains("星期三") || dayPart.contains("wed"))) {
            return true;
        }
        if ((dayName.equals("周四") || dayName.equals("Thursday")) && (dayPart.contains("周四") || dayPart.contains("thursday") || dayPart.contains("星期四") || dayPart.contains("thu"))) {
            return true;
        }
        if ((dayName.equals("周五") || dayName.equals("Friday")) && (dayPart.contains("周五") || dayPart.contains("friday") || dayPart.contains("星期五") || dayPart.contains("fri"))) {
            return true;
        }
        if ((dayName.equals("周六") || dayName.equals("Saturday")) && (dayPart.contains("周六") || dayPart.contains("saturday") || dayPart.contains("星期六") || dayPart.contains("sat"))) {
            return true;
        }
        if ((dayName.equals("周日") || dayName.equals("Sunday")) && (dayPart.contains("周日") || dayPart.contains("sunday") || dayPart.contains("星期日") || dayPart.contains("sun"))) {
            return true;
        }

        if (dayPart.contains("工作日") || dayPart.contains("weekday") || dayPart.contains("周一至周五") || dayPart.contains("Monday-Friday") || dayPart.contains("周一至五")) {
            if (dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY) {
                return true;
            }
        }

        if (dayPart.contains("周末") || dayPart.contains("weekend") || dayPart.contains("周六周日") || dayPart.contains("Saturday-Sunday")) {
            if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
                return true;
            }
        }

        return false;
    }

    private String getDayName(DayOfWeek dayOfWeek) {
        switch (dayOfWeek) {
            case MONDAY: return "周一";
            case TUESDAY: return "周二";
            case WEDNESDAY: return "周三";
            case THURSDAY: return "周四";
            case FRIDAY: return "周五";
            case SATURDAY: return "周六";
            case SUNDAY: return "周日";
            default: return "";
        }
    }
    
    @GetMapping("/appointments")
    public String myAppointments(@RequestParam(defaultValue = "0") int page,
                                 @RequestParam(required = false) String status,
                                 @RequestParam(required = false) String keyword,
                                 HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        Pageable pageable = PageRequest.of(page, 10);
        Page<Appointment> appointmentPage;
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.trim();
            if (status != null && !status.isEmpty()) {
                appointmentPage = appointmentService.searchByUserIdAndStatusAndDoctorName(user.getId(), status, kw, pageable);
            } else {
                appointmentPage = appointmentService.searchByUserIdAndDoctorName(user.getId(), kw, pageable);
            }
        } else if (status != null && !status.isEmpty()) {
            appointmentPage = appointmentService.findByUserIdAndStatus(user.getId(), status, pageable);
        } else {
            appointmentPage = appointmentService.findByUserId(user.getId(), pageable);
        }
        
        model.addAttribute("appointments", appointmentPage.getContent());
        model.addAttribute("appointmentPage", appointmentPage);
        model.addAttribute("user", user);
        model.addAttribute("currentStatus", status);
        model.addAttribute("keyword", keyword);
        return "appointments";
    }
    
    @GetMapping("/appointment/edit")
    public String editAppointment(@RequestParam Long id, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        Appointment appointment = appointmentService.getById(id);
        if (appointment == null || !appointment.getUser().getId().equals(user.getId())) {
            return "redirect:/appointments";
        }
        model.addAttribute("appointment", appointment);
        model.addAttribute("user", user);
        return "appointment-edit";
    }
    
    @PostMapping("/appointment/update")
    public String updateAppointment(@RequestParam Long id,
                                    @RequestParam String appointmentDate,
                                    @RequestParam String appointmentTime,
                                    @RequestParam String patientName,
                                    @RequestParam String patientPhone,
                                    HttpSession session,
                                    Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        Appointment appointment = appointmentService.getById(id);
        if (appointment == null || !appointment.getUser().getId().equals(user.getId())) {
            return "redirect:/appointments";
        }

        if (patientPhone == null || patientPhone.trim().isEmpty()) {
            model.addAttribute("appointment", appointment);
            model.addAttribute("user", user);
            model.addAttribute("error", "联系电话不能为空");
            return "appointment-edit";
        }

        Doctor doctor = appointment.getDoctor();
        LocalDate date = LocalDate.parse(appointmentDate);
        LocalTime time = LocalTime.parse(appointmentTime);

        if (date.isBefore(LocalDate.now()) || (date.isEqual(LocalDate.now()) && time.isBefore(LocalTime.now()))) {
            model.addAttribute("appointment", appointment);
            model.addAttribute("user", user);
            model.addAttribute("error", "不能修改到过去的时间，请选择未来的日期和时间");
            return "appointment-edit";
        }

        if (!isWithinAvailableTime(doctor.getAvailableTime(), date, time)) {
            model.addAttribute("appointment", appointment);
            model.addAttribute("user", user);
            model.addAttribute("error", "预约时间不在医生坐诊时间内，请选择" + doctor.getAvailableTime());
            return "appointment-edit";
        }

        appointment.setAppointmentDate(date);
        appointment.setAppointmentTime(time);
        appointment.setPatientName(patientName);
        appointment.setPatientPhone(patientPhone);

        appointmentService.save(appointment);

        model.addAttribute("message", "修改成功！");
        model.addAttribute("user", user);
        return "appointment-success";
    }
    
    @GetMapping("/appointment/cancel")
    public String cancelAppointment(@RequestParam Long id, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        Appointment appointment = appointmentService.getById(id);
        if (appointment != null && appointment.getUser().getId().equals(user.getId())) {
            appointment.setStatus("已取消");
            appointmentService.save(appointment);
        }
        return "redirect:/appointments";
    }
    
    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        return "profile";
    }
    
    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam String name,
                               @RequestParam(required = false) String phone,
                               @RequestParam(required = false) String oldPassword,
                               @RequestParam(required = false) String newPassword,
                               HttpSession session,
                               Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        // 更新姓名和手机号
        user.setName(name);
        if (phone != null && !phone.isEmpty()) {
            user.setPhone(phone);
        }
        
        // 如果提供了密码，则修改密码
        if (oldPassword != null && !oldPassword.isEmpty() 
            && newPassword != null && !newPassword.isEmpty()) {
            if (!user.getPassword().equals(oldPassword)) {
                model.addAttribute("user", user);
                model.addAttribute("error", "原密码错误");
                return "profile";
            }
            user.setPassword(newPassword);
        }
        
        userService.save(user);
        
        model.addAttribute("user", user);
        model.addAttribute("success", "个人信息更新成功！");
        return "profile";
    }
}
