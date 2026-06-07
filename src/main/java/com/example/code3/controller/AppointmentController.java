package com.example.code3.controller;

import com.example.code3.entity.Appointment;
import com.example.code3.entity.Doctor;
import com.example.code3.entity.MedicalRecord;
import com.example.code3.entity.Review;
import com.example.code3.entity.User;
import com.example.code3.exception.BusinessException;
import com.example.code3.service.AppointmentService;
import com.example.code3.service.DoctorService;
import com.example.code3.service.MedicalRecordService;
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
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Controller
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private UserService userService;

    @Autowired
    private MedicalRecordService medicalRecordService;

    @Autowired
    private ReviewService reviewService;

    @GetMapping("/patient/records")
    public String patientRecords(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        List<MedicalRecord> records = medicalRecordService.findByUserId(user.getId());
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
        // 预解析坐诊时间范围给前端
        String[] timeRange = parseTimeRange(doctor.getAvailableTime());
        model.addAttribute("doctor", doctor);
        model.addAttribute("user", user);
        model.addAttribute("startTime", timeRange[0]);
        model.addAttribute("endTime", timeRange[1]);
        return "appointment-create";
    }

    /**
     * 从坐诊时间文本中提取时段（如 "上午8:00-12:00" → ["08:00", "12:00"]）
     */
    private String[] parseTimeRange(String availableTime) {
        if (availableTime == null || availableTime.trim().isEmpty()) {
            return new String[]{"", ""};
        }
        Pattern p = Pattern.compile("(\\d{1,2}:\\d{2})\\s*[-~]\\s*(\\d{1,2}:\\d{2})");
        Matcher m = p.matcher(availableTime);
        if (m.find()) {
            String start = m.group(1);
            String end = m.group(2);
            if (start.length() == 4) start = "0" + start;
            if (end.length() == 4) end = "0" + end;
            return new String[]{start, end};
        }
        return new String[]{"", ""};
    }

    @GetMapping("/appointment/booked-slots")
    @ResponseBody
    public List<Map<String, String>> getBookedSlots(@RequestParam Long doctorId,
                                                     @RequestParam String date,
                                                     @RequestParam(required = false) Long excludeId) {
        LocalDate localDate = LocalDate.parse(date);
        List<Appointment> appointments = appointmentService.findByDoctorIdAndDate(doctorId, localDate);
        return appointments.stream()
                .filter(a -> ("待就诊".equals(a.getStatus()) || "已完成".equals(a.getStatus()))
                        && (excludeId == null || !a.getId().equals(excludeId)))
                .map(a -> {
                    Map<String, String> m = new HashMap<>();
                    m.put("time", a.getAppointmentTime().format(DateTimeFormatter.ofPattern("HH:mm")));
                    m.put("status", a.getStatus());
                    return m;
                })
                .collect(Collectors.toList());
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

        if (!patientPhone.matches("^1[3-9]\\d{9}$")) {
            Doctor doctor = doctorService.getById(doctorId);
            model.addAttribute("doctor", doctor);
            model.addAttribute("user", user);
            model.addAttribute("error", "请输入有效的11位手机号");
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
        
        // 获取该医生在预约日期的已预约时段（排除自身）
        if (appointment.getDoctor() != null) {
            Long doctorId = appointment.getDoctor().getId();
            LocalDate date = appointment.getAppointmentDate();
            List<Appointment> bookedAppts = appointmentService.findByDoctorIdAndDate(doctorId, date);
            List<String> bookedTimes = bookedAppts.stream()
                .filter(a -> ("待就诊".equals(a.getStatus()) || "已完成".equals(a.getStatus()))
                    && !a.getId().equals(id))
                .map(a -> a.getAppointmentTime().format(DateTimeFormatter.ofPattern("HH:mm")))
                .collect(Collectors.toList());
            model.addAttribute("bookedTimes", bookedTimes);
        } else {
            model.addAttribute("bookedTimes", List.of());
        }
        
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

        if (!patientPhone.matches("^1[3-9]\\d{9}$")) {
            model.addAttribute("appointment", appointment);
            model.addAttribute("user", user);
            model.addAttribute("error", "请输入有效的11位手机号");
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

        // 检查时间冲突（排除自身）
        if (appointmentService.hasTimeConflict(doctor.getId(), date, time, id)) {
            model.addAttribute("appointment", appointment);
            model.addAttribute("user", user);
            model.addAttribute("error", "该时间段已被其他患者预约，请选择其他时间");
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

    // ==================== 患者评价功能 ====================

    @GetMapping("/review/create")
    public String reviewPage(@RequestParam Long appointmentId, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        Appointment appointment = appointmentService.getById(appointmentId);
        if (appointment == null || !appointment.getUser().getId().equals(user.getId())) {
            return "redirect:/appointments";
        }
        if (!"已完成".equals(appointment.getStatus())) {
            return "redirect:/appointments";
        }
        // 检查是否已评价
        Review existing = reviewService.findByAppointmentId(appointmentId);
        if (existing != null) {
            model.addAttribute("error", "您已经对该预约进行过评价");
        }
        model.addAttribute("appointment", appointment);
        model.addAttribute("existing", existing);
        model.addAttribute("user", user);
        return "review-create";
    }

    @PostMapping("/review/submit")
    public String submitReview(@RequestParam Long appointmentId,
                               @RequestParam int rating,
                               @RequestParam(required = false) String comment,
                               HttpSession session,
                               Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        Appointment appointment = appointmentService.getById(appointmentId);
        if (appointment == null || !appointment.getUser().getId().equals(user.getId())) {
            return "redirect:/appointments";
        }
        if (!"已完成".equals(appointment.getStatus())) {
            return "redirect:/appointments";
        }
        // 防止重复评价
        Review existing = reviewService.findByAppointmentId(appointmentId);
        if (existing != null) {
            model.addAttribute("error", "您已经评价过，不能重复提交");
            model.addAttribute("appointment", appointment);
            model.addAttribute("existing", existing);
            model.addAttribute("user", user);
            return "review-create";
        }

        if (rating < 1 || rating > 5) {
            model.addAttribute("error", "评分必须在1-5星之间");
            model.addAttribute("appointment", appointment);
            model.addAttribute("user", user);
            return "review-create";
        }

        Review review = new Review();
        review.setAppointment(appointment);
        review.setUser(user);
        review.setDoctor(appointment.getDoctor());
        review.setRating(rating);
        review.setComment(comment != null && !comment.trim().isEmpty() ? comment.trim() : null);
        review.setCreatedAt(java.time.LocalDateTime.now());
        reviewService.save(review);

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

        user.setName(name);
        if (phone != null && !phone.isEmpty()) {
            if (!phone.matches("^1[3-9]\\d{9}$")) {
                model.addAttribute("user", user);
                model.addAttribute("error", "请输入有效的11位手机号");
                return "profile";
            }
            user.setPhone(phone);
        }

        if (oldPassword != null && !oldPassword.isEmpty()
            && newPassword != null && !newPassword.isEmpty()) {
            if (newPassword.length() < 6 || newPassword.length() > 20) {
                model.addAttribute("user", user);
                model.addAttribute("error", "新密码长度应为6-20位");
                return "profile";
            }
            if (!userService.checkPassword(user, oldPassword)) {
                model.addAttribute("user", user);
                model.addAttribute("error", "原密码错误");
                return "profile";
            }
            userService.updatePassword(user, newPassword);
        } else {
            userService.save(user);
        }

        // 重新加载用户信息
        user = userService.getById(user.getId());
        session.setAttribute("user", user);

        model.addAttribute("user", user);
        model.addAttribute("success", "个人信息更新成功！");
        return "profile";
    }
}
