-- 插入测试用户数据
USE rj2411260;

-- 清空现有数据（可选）
DELETE FROM user;

-- 插入患者用户 (role = 0)
INSERT INTO user (username, password, name, phone, role) VALUES 
('user1', '123456', '张三', '13800001001', 0),
('user2', '123456', '李四', '13800001002', 0),
('user3', '123456', '王五', '13800001003', 0);

-- 插入管理员用户 (role = 1)
INSERT INTO user (username, password, name, phone, role) VALUES 
('admin', 'admin', '系统管理员', '13800009999', 1);

-- 插入医生用户 (role = 2)
INSERT INTO user (username, password, name, phone, role) VALUES 
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
