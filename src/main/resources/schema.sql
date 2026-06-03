CREATE DATABASE IF NOT EXISTS rj2411260 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE rj2411260;

CREATE TABLE IF NOT EXISTS user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    phone VARCHAR(20) UNIQUE COMMENT '手机号',
    role INT NOT NULL DEFAULT 0 COMMENT '角色 0-患者 1-管理员 2-医生',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

CREATE TABLE IF NOT EXISTS department (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '科室ID',
    name VARCHAR(50) UNIQUE NOT NULL COMMENT '科室名称',
    description VARCHAR(200) COMMENT '科室描述',
    active BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='科室表';

CREATE TABLE IF NOT EXISTS doctor (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '医生ID',
    name VARCHAR(50) NOT NULL COMMENT '医生姓名',
    title VARCHAR(50) NOT NULL COMMENT '医生职称',
    department_id BIGINT NOT NULL COMMENT '所属科室ID',
    description VARCHAR(500) COMMENT '医生简介',
    available_time VARCHAR(200) NOT NULL COMMENT '坐诊时间',
    active BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (department_id) REFERENCES department(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='医生表';

CREATE TABLE IF NOT EXISTS appointment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '预约ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    doctor_id BIGINT NOT NULL COMMENT '医生ID',
    appointment_date DATE NOT NULL COMMENT '预约日期',
    appointment_time TIME NOT NULL COMMENT '预约时间',
    status VARCHAR(20) NOT NULL DEFAULT '待就诊' COMMENT '预约状态',
    patient_name VARCHAR(50) NOT NULL COMMENT '患者姓名',
    patient_id_card VARCHAR(18) COMMENT '患者身份证号',
    patient_phone VARCHAR(20) COMMENT '患者联系电话',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES doctor(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预约挂号表';

CREATE TABLE IF NOT EXISTS review (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评价ID',
    appointment_id BIGINT NOT NULL COMMENT '预约ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    doctor_id BIGINT NOT NULL COMMENT '医生ID',
    rating INT NOT NULL COMMENT '评分 1-5',
    comment VARCHAR(500) COMMENT '评论内容',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (appointment_id) REFERENCES appointment(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES doctor(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价表';

-- ==================== 初始数据 ====================

-- 用户数据
INSERT IGNORE INTO user (username, password, name, phone, role) VALUES
-- 患者 (role=0)
('user1',   '123456', '张三', '13800001001', 0),
('user2',   '123456', '李四', '13800001002', 0),
('user3',   '123456', '王五', '13800001003', 0),
-- 管理员 (role=1)
('admin',   'admin',  '管理员',  '13900139001', 1),
-- 医生账号 (role=2)
('wangdoctor', '123456', '王医生', NULL, 2),
('lidoctor', '123456', '李医生', NULL, 2),
('zhangdoctor', '123456', '张医生', NULL, 2),
('liudoctor', '123456', '刘医生', NULL, 2),
('chendoctor', '123456', '陈医生', NULL, 2),
('zhaodoctor', '123456', '赵医生', NULL, 2),
('sundoctor', '123456', '孙医生', NULL, 2),
('zhoudoctor', '123456', '周医生', NULL, 2),
('wudoctor', '123456', '吴医生', NULL, 2),
('zhendoctor', '123456', '郑医生', NULL, 2),
('fengdoctor', '123456', '冯医生', NULL, 2);

-- 科室数据
INSERT IGNORE INTO department (name, description) VALUES
('内科',     '主要诊治呼吸系统、消化系统等疾病'),
('外科',     '主要诊治外伤、肿瘤等外科疾病'),
('妇产科',   '主要诊治妇科疾病及产科服务'),
('儿科',     '主要诊治儿童常见疾病'),
('骨科',     '主要诊治骨骼关节等疾病'),
('眼科',     '主要诊治眼部疾病'),
('耳鼻喉科', '主要诊治耳、鼻、喉疾病'),
('皮肤科',   '主要诊治皮肤相关疾病'),
('口腔科',   '主要诊治口腔相关疾病');

-- 医生数据（依赖科室ID，按插入顺序内科=1,外科=2,妇产科=3,儿科=4,骨科=5）
INSERT IGNORE INTO doctor (name, title, department_id, description, available_time) VALUES
('王医生', '主任医师',   1, '擅长呼吸系统疾病诊治，从事临床工作30年',               '周一、周三、周五 上午8:00-12:00'),
('李医生', '副主任医师', 1, '擅长心血管疾病诊治，临床经验丰富',                   '周二、周四 上午8:00-12:00'),
('张医生', '主治医师',   1, '擅长消化系统疾病诊治',                               '周一至周五 下午14:00-18:00'),
('刘医生', '主任医师',   2, '擅长普外科手术，经验丰富',                           '周一、周三、周五 上午8:00-12:00'),
('陈医生', '副主任医师', 2, '擅长骨科创伤治疗',                                   '周二、周四 上午8:00-12:00'),
('赵医生', '主任医师',   3, '擅长妇科肿瘤诊治',                                   '周一、周三 上午8:00-12:00'),
('孙医生', '副主任医师', 3, '擅长产科护理与分娩指导',                             '周二、周四、周五 上午8:00-12:00'),
('周医生', '主任医师',   4, '擅长小儿呼吸系统疾病',                               '周一至周五 上午8:00-12:00'),
('吴医生', '主治医师',   4, '擅长小儿消化系统疾病',                               '周一至周五 下午14:00-18:00'),
('郑医生', '主任医师',   5, '擅长关节置换手术',                                   '周一、周三 上午8:00-12:00'),
('冯医生', '副主任医师', 5, '擅长脊柱疾病诊治',                                   '周二、周四 上午8:00-12:00');