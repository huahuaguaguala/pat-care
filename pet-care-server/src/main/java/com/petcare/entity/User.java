package com.petcare.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String openid;
    private String username;
    private String password;
    private String nickname;
    private String realName;
    private String phone;
    private Integer gender;
    private LocalDate birthday;
    private String avatar;
    private Integer role;
    private Long storeId;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    private Long createUser;
    private Long updateUser;
}
