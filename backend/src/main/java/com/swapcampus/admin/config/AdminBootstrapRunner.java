package com.swapcampus.admin.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.swapcampus.common.enums.Role;
import com.swapcampus.common.enums.UserStatus;
import com.swapcampus.user.entity.UserEntity;
import com.swapcampus.user.entity.UserProfileEntity;
import com.swapcampus.user.mapper.UserMapper;
import com.swapcampus.user.mapper.UserProfileMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminBootstrapRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrapRunner.class);
    private static final String ADMIN_USERNAME = "reviewer";
    private static final String ADMIN_PASSWORD = "Admin1234!";
    private static final String ADMIN_REAL_NAME = "平台审核员";
    private static final String PRODUCT_REVIEWER_USERNAME = "product_reviewer";
    private static final String PRODUCT_REVIEWER_PASSWORD = "Product1234!";
    private static final String PRODUCT_REVIEWER_REAL_NAME = "商品审核员";

    private final UserMapper userMapper;
    private final UserProfileMapper userProfileMapper;
    private final PasswordEncoder passwordEncoder;

    public AdminBootstrapRunner(UserMapper userMapper,
                                UserProfileMapper userProfileMapper,
                                PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.userProfileMapper = userProfileMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        createAccountIfMissing(ADMIN_USERNAME, ADMIN_PASSWORD, ADMIN_REAL_NAME, Role.ADMIN);
        createAccountIfMissing(
                PRODUCT_REVIEWER_USERNAME,
                PRODUCT_REVIEWER_PASSWORD,
                PRODUCT_REVIEWER_REAL_NAME,
                Role.PRODUCT_REVIEWER
        );
    }

    private void createAccountIfMissing(String username, String password, String realName, Role role) {
        UserEntity existing = userMapper.selectOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getUsername, username));
        if (existing != null) {
            return;
        }

        UserEntity admin = new UserEntity();
        admin.setUsername(username);
        admin.setPasswordHash(passwordEncoder.encode(password));
        admin.setRole(role);
        admin.setStatus(UserStatus.ACTIVE);
        admin.setCreditScore(100);
        admin.setPointBalance(0);
        admin.setIsDeleted(false);
        userMapper.insert(admin);

        UserProfileEntity profile = new UserProfileEntity();
        profile.setUserId(admin.getId());
        profile.setRealName(realName);
        userProfileMapper.insert(profile);

        log.info("Created default admin account: username={}, password={}", username, password);
    }
}
