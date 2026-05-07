package com.xinjiema.hualimall.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    private String username;
    private String password;
    private String nickname;
    private String avatarUrl;
    private String role;

    private String realName;
    private String phone;

    private String shopName;
    private String businessLicenseNo;
    private String contactName;
    private String contactPhone;
}
