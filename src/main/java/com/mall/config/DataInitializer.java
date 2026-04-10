package com.mall.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.entity.User;
import com.mall.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 数据初始化器 - 创建默认管理员账号
 */
@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        initAdminUser();
    }

    /**
     * 初始化管理员账号
     */
    private void initAdminUser() {
        // 检查管理员是否已存在
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, "admin"));
        
        if (count == 0) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setNickname("系统管理员");
            admin.setAvatar("https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png");
            admin.setEmail("admin@mall.com");
            admin.setPhone("13800000000");
            admin.setGender(1);
            admin.setRole(1); // 管理员
            admin.setStatus(1);
            userMapper.insert(admin);
            log.info("========================================");
            log.info("默认管理员账号创建成功！");
            log.info("用户名: admin");
            log.info("密码: admin123");
            log.info("========================================");
        } else {
            log.info("管理员账号已存在，跳过初始化");
        }
    }
}
