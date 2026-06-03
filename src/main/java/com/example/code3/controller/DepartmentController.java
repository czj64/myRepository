package com.example.code3.controller;

import com.example.code3.entity.Department;
import com.example.code3.entity.User;
import com.example.code3.service.DepartmentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class DepartmentController {
    
    @Autowired
    private DepartmentService departmentService;
    
    @GetMapping("/departments")
    public String departments(@RequestParam(required = false) String keyword,
                              HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        List<Department> departments = departmentService.findActiveDepartments();
        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.trim().toLowerCase();
            departments = departments.stream()
                .filter(d -> d.getName().toLowerCase().contains(kw) ||
                             (d.getDescription() != null && d.getDescription().toLowerCase().contains(kw)))
                .collect(Collectors.toList());
        }
        model.addAttribute("departments", departments);
        model.addAttribute("user", user);
        model.addAttribute("keyword", keyword);
        return "departments";
    }
}
