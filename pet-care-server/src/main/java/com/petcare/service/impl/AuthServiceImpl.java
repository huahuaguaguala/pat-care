package com.petcare.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petcare.common.Result;
import com.petcare.dto.LoginDTO;
import com.petcare.entity.User;
import com.petcare.mapper.UserMapper;
import com.petcare.service.AuthService;
import com.petcare.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class AuthServiceImpl implements AuthService {
    @Autowired private UserMapper userMapper;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtUtils jwtUtils;

    @Override
    public Result<?> wechatLogin(LoginDTO dto) {
        if (dto.getOpenid() == null || dto.getOpenid().isEmpty()) return Result.fail("openid required");
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getOpenid, dto.getOpenid()));
        if (user == null) {
            user = new User();
            user.setOpenid(dto.getOpenid());
            user.setRole(0);
            user.setNickname("User" + System.currentTimeMillis() % 10000);
            user.setStatus(1);
            userMapper.insert(user);
        }
        return buildTokenResult(user);
    }

    @Override
    public Result<?> login(LoginDTO dto) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (user == null) return Result.fail("Account not found");
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) return Result.fail("Wrong password");
        if (user.getStatus() == 0) return Result.fail("Account disabled");
        return buildTokenResult(user);
    }

    private Result<?> buildTokenResult(User user) {
        String token = jwtUtils.generateToken(user.getId(), user.getRole(), user.getStoreId());
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", user);
        return Result.success(result);
    }
}
