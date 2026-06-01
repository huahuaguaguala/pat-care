package com.petcare.config;

import com.petcare.interceptor.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired private JwtInterceptor jwtInterceptor;
    @Autowired private AuditInterceptor auditInterceptor;
    @Autowired private RoleInterceptor roleInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 1. JWT: parse token -> set userId/role on request attributes
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                    "/api/auth/login", "/api/auth/wechat-login", "/api/auth/register",
                    "/api/service/category", "/api/service/item", "/api/service/item/*",
                    "/api/rank/pet/weekly", "/api/rank/service/hot",
                    "/api/breed", "/api/package", "/api/package/*"
                );

        // 2. Role: check @RequireRole annotation (depends on JWT above)
        registry.addInterceptor(roleInterceptor)
                .addPathPatterns("/api/**");

        // 3. Audit: log all API calls
        registry.addInterceptor(auditInterceptor)
                .addPathPatterns("/api/**");
    }
}
