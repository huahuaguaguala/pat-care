package com.petcare.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
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
    private String phone;
    private String avatar;
    /** 0=宠物主 1=店员 2=店长 */
    private Integer role;
    private Long storeId;
    /** 1=正常 0=禁用 */
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
