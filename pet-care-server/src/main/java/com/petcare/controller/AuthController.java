package com.petcare.controller;

import com.petcare.common.Result;
import com.petcare.dto.LoginDTO;
import com.petcare.entity.User;
import com.petcare.mapper.UserMapper;
import com.petcare.utils.JwtUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired private UserMapper userMapper;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtUtils jwtUtils;

    /** 微信登录（宠物主） */
    @PostMapping("/wechat-login")
    public Result<?> wechatLogin(@RequestBody LoginDTO dto) {
        if (dto.getOpenid() == null || dto.getOpenid().isEmpty()) {
            return Result.fail("openid不能为空");
        }
        // 查找已有用户
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getOpenid, dto.getOpenid()));
        if (user == null) {
            // 新用户自动注册
            user = new User();
            user.setOpenid(dto.getOpenid());
            user.setRole(0); // 宠物主
            user.setNickname("宠友" + System.currentTimeMillis() % 10000);
            user.setStatus(1);
            userMapper.insert(user);
        }
        String token = jwtUtils.generateToken(user.getId(), user.getRole(), user.getStoreId());
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", user);
        return Result.success(result);
    }

    /** 账号密码登录（店员/店长） */
    @PostMapping("/login")
    public Result<?> login(@RequestBody LoginDTO dto) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (user == null) {
            return Result.fail("账号不存在");
        }
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            return Result.fail("密码错误");
        }
        if (user.getStatus() == 0) {
            return Result.fail("该账号已被禁用");
        }
        String token = jwtUtils.generateToken(user.getId(), user.getRole(), user.getStoreId());
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", user);
        return Result.success(result);
    }
}
