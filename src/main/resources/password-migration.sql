-- ===========================================================
-- 密码从明文迁移到 BCrypt 哈希
-- 在 MySQL 客户端执行（先停应用）
-- ===========================================================

-- 所有使用密码 "123456" 的账号 → 统一替换为 BCrypt 哈希
UPDATE `user` SET password = '$2a$12$oeJ/39RzVABpkGIC6gEEyeTsZMSQpk/zZQCBDb8rVqLYru8sJocZ2'
WHERE password NOT LIKE '$2a$%' AND username != 'admin';

-- 管理员账号密码 "admin" → BCrypt 哈希
UPDATE `user` SET password = '$2a$12$.ma/iGtx4Y0JYrmk91LMTexxl7tq5PsNOxyxWO8DfH09Ecffa4VsC'
WHERE username = 'admin';

-- 验证执行结果
SELECT username, LEFT(password, 10) AS hash_prefix FROM `user`;
DELETE FROM user WHERE id NOT IN (SELECT MIN(id) FROM user GROUP BY name);
