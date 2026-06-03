package com.example.code3.config;

import com.example.code3.entity.Department;
import com.example.code3.entity.Doctor;
import com.example.code3.entity.User;
import com.example.code3.repository.DepartmentRepository;
import com.example.code3.repository.DoctorRepository;
import com.example.code3.repository.UserRepository;
import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// @Component  // 已关闭，数据库已有数据时不再需要自动初始化
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private DoctorRepository doctorRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        initUsers();
        initDepartments();
        initDoctors();
        initDoctorUsers();
        
        entityManager.createNativeQuery("UPDATE department SET active = true WHERE active IS NULL").executeUpdate();
        entityManager.createNativeQuery("UPDATE doctor SET active = true WHERE active IS NULL").executeUpdate();
    }
    
    private void initUsers() {
        if (userRepository.findByUsername("user001").isEmpty()) {
            User user1 = new User();
            user1.setUsername("user001");
            user1.setPassword("123456");
            user1.setName("张三");
            user1.setPhone("13800138001");
            user1.setRole(0);
            userRepository.save(user1);
        }
        
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword("admin");
            admin.setName("管理员");
            admin.setPhone("13900139001");
            admin.setRole(1);
            userRepository.save(admin);
        }
        
        if (userRepository.findByUsername("user002").isEmpty()) {
            User user2 = new User();
            user2.setUsername("user002");
            user2.setPassword("123456");
            user2.setName("李四");
            user2.setPhone("13800138002");
            user2.setRole(0);
            userRepository.save(user2);
        }
    }
    
    private void initDepartments() {
        String[] departments = {"内科", "外科", "妇产科", "儿科", "骨科", "眼科", "耳鼻喉科", "皮肤科", "口腔科"};
        String[] descriptions = {
            "主要诊治呼吸系统、消化系统等疾病",
            "主要诊治外伤、肿瘤等外科疾病",
            "主要诊治妇科疾病及产科服务",
            "主要诊治儿童常见疾病",
            "主要诊治骨骼关节等疾病",
            "主要诊治眼部疾病",
            "主要诊治耳、鼻、喉疾病",
            "主要诊治皮肤相关疾病",
            "主要诊治口腔相关疾病"
        };
        
        for (int i = 0; i < departments.length; i++) {
            if (departmentRepository.findByName(departments[i]).isEmpty()) {
                Department dept = new Department();
                dept.setName(departments[i]);
                dept.setDescription(descriptions[i]);
                departmentRepository.save(dept);
            }
        }
    }
    
    private void initDoctors() {
        // 清理引用了不存在医生的孤儿预约
        entityManager.createNativeQuery("DELETE r FROM review r INNER JOIN appointment a ON r.appointment_id = a.id LEFT JOIN doctor d ON a.doctor_id = d.id WHERE d.id IS NULL").executeUpdate();
        entityManager.createNativeQuery("DELETE a FROM appointment a LEFT JOIN doctor d ON a.doctor_id = d.id WHERE d.id IS NULL").executeUpdate();

        Department neike = departmentRepository.findByName("内科").orElse(null);
        Department waike = departmentRepository.findByName("外科").orElse(null);
        Department fuke = departmentRepository.findByName("妇产科").orElse(null);
        Department erke = departmentRepository.findByName("儿科").orElse(null);
        Department guke = departmentRepository.findByName("骨科").orElse(null);
        
        if (neike != null) {
            createDoctor("王医生", "主任医师", neike, "擅长呼吸系统疾病诊治，从事临床工作30年", "周一、周三、周五 上午8:00-12:00");
            createDoctor("李医生", "副主任医师", neike, "擅长心血管疾病诊治，临床经验丰富", "周二、周四 上午8:00-12:00");
            createDoctor("张医生", "主治医师", neike, "擅长消化系统疾病诊治", "周一至周五 下午14:00-18:00");
        }
        
        if (waike != null) {
            createDoctor("刘医生", "主任医师", waike, "擅长普外科手术，经验丰富", "周一、周三、周五 上午8:00-12:00");
            createDoctor("陈医生", "副主任医师", waike, "擅长骨科创伤治疗", "周二、周四 上午8:00-12:00");
        }
        
        if (fuke != null) {
            createDoctor("赵医生", "主任医师", fuke, "擅长妇科肿瘤诊治", "周一、周三 上午8:00-12:00");
            createDoctor("孙医生", "副主任医师", fuke, "擅长产科护理与分娩指导", "周二、周四、周五 上午8:00-12:00");
        }
        
        if (erke != null) {
            createDoctor("周医生", "主任医师", erke, "擅长小儿呼吸系统疾病", "周一至周五 上午8:00-12:00");
            createDoctor("吴医生", "主治医师", erke, "擅长小儿消化系统疾病", "周一至周五 下午14:00-18:00");
        }
        
        if (guke != null) {
            createDoctor("郑医生", "主任医师", guke, "擅长关节置换手术", "周一、周三 上午8:00-12:00");
            createDoctor("冯医生", "副主任医师", guke, "擅长脊柱疾病诊治", "周二、周四 上午8:00-12:00");
        }
    }
    
    private void initDoctorUsers() {
        String[][] doctors = {
            {"doctor1", "王医生"}, {"doctor2", "李医生"}, {"doctor3", "张医生"},
            {"doctor4", "刘医生"}, {"doctor5", "陈医生"}, {"doctor6", "赵医生"},
            {"doctor7", "孙医生"}, {"doctor8", "周医生"}, {"doctor9", "吴医生"},
            {"doctor10", "郑医生"}, {"doctor11", "冯医生"}
        };
        for (String[] d : doctors) {
            if (userRepository.findByUsername(d[0]).isEmpty()) {
                User doctorUser = new User();
                doctorUser.setUsername(d[0]);
                doctorUser.setPassword("123456");
                doctorUser.setName(d[1]);
                doctorUser.setPhone(null);
                doctorUser.setRole(2);
                userRepository.save(doctorUser);
            }
        }
    }

    private void createDoctor(String name, String title, Department dept, String desc, String time) {
        List<Doctor> existing = doctorRepository.findByDepartmentId(dept.getId());
        for (Doctor d : existing) {
            if (d.getName().equals(name)) {
                return;
            }
        }
        Doctor doctor = new Doctor();
        doctor.setName(name);
        doctor.setTitle(title);
        doctor.setDepartment(dept);
        doctor.setDescription(desc);
        doctor.setAvailableTime(time);
        doctorRepository.save(doctor);
    }
}
