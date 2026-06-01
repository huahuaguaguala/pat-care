package com.petcare.config;

import com.petcare.interceptor.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/login",
                        "/api/auth/wechat-login",
                        "/api/auth/register",
                        "/api/service/category",
                        "/api/service/item",
                        "/api/service/item/*",
                        "/api/rank/pet/weekly",
                        "/api/rank/service/hot",
                        "/api/breed",
                        "/api/package",
                        "/api/package/*",
                        "/api/health/pet/*/timeline",
                        "/api/weight/pet/*",
                        "/api/payment/qrcode/*",
                        "/api/payment/scan/*",
                        "/api/weight/pet/*",
                        "/api/health/pet/*/timeline",
                        "/api/breed",
                        "/api/vaccine/pet/*",
                        "/api/boarding/pet/*"
                );
    }
}
