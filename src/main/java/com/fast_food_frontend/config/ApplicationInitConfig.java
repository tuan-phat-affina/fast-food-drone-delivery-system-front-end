package com.fast_food_frontend.config;

import com.fast_food_frontend.common.PredefinedRole;
import com.fast_food_frontend.entity.Role;
import com.fast_food_frontend.entity.User;
import com.fast_food_frontend.enums.UserStatus;
import com.fast_food_frontend.repository.RoleRepository;
import com.fast_food_frontend.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.HashSet;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Configuration
public class ApplicationInitConfig {

    PasswordEncoder passwordEncoder;

    @NonFinal
    static final String ADMIN_USER = "admin";

    @NonFinal
    static final String ADMIN_PASSWORD = "admin";

    @NonFinal
    static final String ADMIN_ID = "0898454043";


    @Bean
    @ConditionalOnProperty(
            prefix = "spring",
            value = "datasource.driverClassName",
            havingValue = "com.mysql.cj.jdbc.Driver"
    )
    ApplicationRunner applicationRunner(UserRepository userRepository, RoleRepository roleRepository) {
        return args -> {
           if(userRepository.findByUsername(ADMIN_USER).isEmpty()) {
               roleRepository.save(Role.builder()
                               .name(PredefinedRole.USER_ROLE)
                               .description("User Role")
                       .build());
               Role adminRole = roleRepository.save(Role.builder()
                       .name(PredefinedRole.ADMIN_ROLE)
                       .description("Admin Role")
                       .build());

               var role = new HashSet<Role>();
               role.add(adminRole);
               User user = User.builder()
                       .id(Long.valueOf(ADMIN_ID))
                       .username(ADMIN_USER)
                       .password(passwordEncoder.encode(ADMIN_PASSWORD))
                       .role(role)
                       .fullName("Trần Tuấn Phát")
                       .email("tuanphat17edu@gmail.com")
                       .status(UserStatus.ACTIVE.name())
                       .createdAt(Instant.now())
                       .updatedAt(Instant.now())
                       .build();

               userRepository.save(user);
                log.warn("admin user has been created with default password: admin");
           }
        };
    }
}
