package com.petcare.service;
import com.petcare.common.Result;
import com.petcare.dto.LoginDTO;

public interface AuthService {
    Result<?> wechatLogin(LoginDTO dto);
    Result<?> login(LoginDTO dto);
}
