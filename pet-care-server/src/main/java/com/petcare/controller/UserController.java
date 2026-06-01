package com.petcare.controller;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petcare.common.Result;
import com.petcare.entity.User;
import com.petcare.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired private UserMapper userMapper;
    @Autowired private PasswordEncoder passwordEncoder;

    @GetMapping("/me")
    public Result<?> me(HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");
        return Result.success(userMapper.selectById(userId));
    }

    @PutMapping("/me")
    public Result<?> updateMe(@RequestBody User user, HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");
        user.setId(userId);
        user.setPassword(null); // 不允许直接更新密码
        user.setRole(null);     // 不允许改角色
        userMapper.updateById(user);
        return Result.success(userMapper.selectById(userId));
    }

    @GetMapping("/staff")
    public Result<?> staffList(HttpServletRequest req) {
        Integer role = (Integer) req.getAttribute("role");
        if (role != 2) return Result.fail(403, "仅店长可操作");
        List<User> staff = userMapper.selectList(
                new LambdaQueryWrapper<User>().eq(User::getRole, 1));
        return Result.success(staff);
    }

    @PostMapping("/staff")
    public Result<?> addStaff(@RequestBody User staff, HttpServletRequest req) {
        Integer role = (Integer) req.getAttribute("role");
        if (role != 2) return Result.fail(403, "仅店长可操作");
        staff.setPassword(passwordEncoder.encode(staff.getPassword()));
        staff.setRole(1);
        staff.setStatus(1);
        userMapper.insert(staff);
        return Result.success(staff);
    }

    @PutMapping("/staff/{id}/status")
    public Result<?> toggleStaff(@PathVariable Long id, @RequestParam Integer status, HttpServletRequest req) {
        Integer role = (Integer) req.getAttribute("role");
        if (role != 2) return Result.fail(403, "仅店长可操作");
        User staff = new User();
        staff.setId(id);
        staff.setStatus(status);
        userMapper.updateById(staff);
        return Result.success();
    }
}
