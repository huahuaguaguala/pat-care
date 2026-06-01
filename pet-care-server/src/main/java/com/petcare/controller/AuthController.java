package com.petcare.controller;

import com.petcare.common.Result;
import com.petcare.dto.LoginDTO;
import com.petcare.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired private AuthService authService;

    @PostMapping("/wechat-login")
    public Result<?> wechatLogin(@RequestBody LoginDTO dto) {
        return authService.wechatLogin(dto);
    }

    @PostMapping("/login")
    public Result<?> login(@RequestBody LoginDTO dto) {
        return authService.login(dto);
    }
}
