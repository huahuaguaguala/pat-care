package com.petcare.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class LoginDTO {
    /** 微信登录：传 openid；账号登录：传 username */
    private String openid;
    private String username;

    /** 账号密码登录必填 */
    private String password;

    /** 角色：仅在微信登录时使用 */
    private Integer role;
}
