# IDEA项目文档

## 1. 项目基本信息

| 项目属性 | 内容 |
|----------|------|
| 项目名称 | Hospital Appointment System（医院预约挂号系统） |
| 项目路径 | D:\code\code\java\code3 |
| JDK版本 | JDK 17 |
| Maven版本 | Maven 3.8+（使用项目内嵌 mvnw.cmd） |
| 服务器端口 | 23160 |
| 工程类型 | Spring Boot 4.0.6 + Thymeleaf |

## 2. IDEA配置说明

### 2.1 JDK配置
确保IDEA中配置了 JDK 17：
```
File → Project Structure → Project SDK → 选择 JDK 17
File → Project Structure → Project Language Level → 17
```

### 2.2 Maven配置
```
File → Settings → Build, Execution, Deployment → Build Tools → Maven
  → Maven home path：使用项目内嵌的 mvnw（或系统安装的 Maven 3.8+）
  → User settings file：默认
  → Local repository：默认
```

### 2.3 项目SDK
```
File → Project Structure → Project Settings → Project
  → Project SDK：选择 JDK 17
  → Project language level：SDK default (17)
```

## 3. 运行配置

### 3.1 创建 Spring Boot 运行配置
```
Run → Edit Configurations → + → Spring Boot
  → Main class：com.example.code3.Code3Application
  → VM options：(空)
  → Active profiles：(空)
  → Environment variables：(空)
  → JRE：JDK 17
  → Working directory：D:\code\code\java\code3
```

### 3.2 命令行运行
```bash
# 使用 Maven wrapper 运行（Windows）
.\mvnw.cmd spring-boot:run

# 先打包再运行
.\mvnw.cmd clean package
java -jar target\code3.jar

# 跳过测试打包
.\mvnw.cmd clean package -DskipTests
```

## 4. 项目模块详解

### 4.1 源代码目录（src/main/java）

```
com.example.code3
├── Code3Application.java          # Spring Boot 启动类
├──
├── config/                        # 配置目录
│   ├── DataInitializer.java       # 数据初始化器（已关闭）
│   └── GlobalExceptionHandler.java    # 全局异常处理器
├── controller/                    # 控制器
│   ├── HomeController.java        # 首页/登录/注册（6个接口）
│   ├── AppointmentController.java # 患者预约（11个接口）
│   ├── DepartmentController.java  # 科室浏览（1个接口）
│   ├── DoctorController.java      # 医生端（12个接口）
│   └── AdminController.java       # 管理员端（15个接口）
├── entity/                        # JPA实体
│   ├── User.java
│   ├── Department.java
│   ├── Doctor.java
│   ├── Appointment.java
│   └── Review.java
├── enums/
│   └── AppAppointmentStatus.java      # 预约状态常量
├── exception/
│   └── BusinessException.java     # 业务异常
├── repository/                    # 数据访问层
│   ├── UserRepository.java
│   ├── DepartmentRepository.java
│   ├── DoctorRepository.java
│   ├── AppointmentRepository.java
│   └── ReviewRepository.java
└── service/                      # 服务层
    ├── impl/                     # 实现类
    │   ├── UserServiceImpl.java
    │   ├── DepartmentServiceImpl.java
    │   ├── DoctorServiceImpl.java
    │   ├── AppointmentServiceImpl.java
    │   └── ReviewServiceImpl.java
    ├── UserService.java
    ├── DepartmentService.java
    ├── DoctorService.java
    ├── AppointmentService.java
    └── ReviewService.java
```

### 4.2 资源目录（src/main/resources）

```
resources/
├── templates/                    # Thymeleaf模板（30个HTML）
│   ├── index.html               # 首页
│   ├── login.html / register.html / error.html
│   ├── admin-login.html
│   ├── doctor-login.html
│   ├── departments.html / doctors.html / doctor-detail.html
│   ├── appointment-create.html / appointment-edit.html
│   ├── appointment-success.html / appointments.html
│   ├── patient-records.html / profile.html
│   ├── admin-dashboard.html管理端仪表板
│   ├── admin-departments.html管理端/ admin-department-form.html
│   ├── admin-doctors.html管理/ admin-doctor-form.html
│   ├── admin-appointments.html
│   ├── admin-users.html
│   ├── admin-statistics.html
│   ├── doctor-dashboard.html
│   ├── doctor-patients.html / doctor-records.html
│   ├── doctor-record-edit.html
│   ├── doctor-schedule.html / doctor-messages.html
│   └── doctor-profile.html
├── static/                       # 静态资源（空）
├── schema.sql                    # 数据库建表+初始数据
└── application.properties        # 应用配置
```

### 4.3 项目根目录

```
code3/
├── src/                          # 源代码
├── docs/                         # 项目文档
├── pom.xml                       # Maven依赖配置
├── mvnw / mvnw.cmd              # Maven Wrapper
├── .mvn/                         # Maven配置
└──
└── target/                       # 构建输出
```

##  |  
```

## 5. 关键技术点

### 5.1 Thymeleaf模板
- 模板前缀：`classpath:/templates/`
- 模板后缀：`.html`
- 缓存：关闭（开发环境）
- 表达式：`[[${...}]]` 或 `th:text`
- 判断：`th:if`, `th:unless`
- 循环：`th:each`
- URL：`th:href="@{...}"`

### 5.2 JPA/Hibernate
- DDL策略：`spring.jpa.hibernate.ddl-auto=update`
- 方言：`MySQLDialect`
- 自动建表：基于实体类注解（@Entity, @Table）
- 查询方法：基于方法命名规则（findByXXX）

### 5.3 会话管理
```java
// 存储
HttpSession session
session.setAttribute("user", user)

// 读取
User user = (User) session.getAttribute("user")

// 销毁
session.invalidate()
```

### 5.4 异常处理
```java
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class) → 错误页面
    @ExceptionHandler(Exception.class) → 500页面
}
```

## 6. 常用IDEA快捷键

| 快捷键 | 功能 |
|--------|------|
| Ctrl+N | 查找类 |
| Ctrl+Shift+N | 查找文件 |
| Ctrl+E | 最近文件 |
| Alt+Insert | 生成代码 |
| Ctrl+Alt+L | 格式化代码 |
| Ctrl+Shift+F | 全局搜索 |
| Shift+Shift | 全局搜索（任何地方） |
| F12 | 打开终端 |
| Ctrl+F12 | 文件结构 |

## 7. 调试说明

### 7.1 断点调试
1. 在代码行号旁点击设置断点
2. 以Debug模式运行项目
3. 访问对应的URL触发断点
4. 使用 F8（步过）/ F7（步入）调试

### 7.2 查看SQL执行
application.properties中已开启：
```properties
spring.jpa.show-sql=true``` 开启SQL日志，可在控制台查看JPA生成的SQL语句。

## 8. 数据库连接配置

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/rj2411260?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
spring.datasource.username=student
spring.datasource.password=Student_123
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
```

## 9. 访问入口

| 角色 | 登录地址 | 默认账号 |
|------|----------|----------|
| 患者 | http://localhost:23160/login | user1 / 123456 |
| 管理员 | http://localhost:23160/admin-login | admin / admin |
| 医生 | http://localhost:23160/doctor-login | wangdoctor / 123456 |