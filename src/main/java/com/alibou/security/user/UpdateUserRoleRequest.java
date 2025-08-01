package com.alibou.security.user;

import lombok.Data;

@Data
public class UpdateUserRoleRequest {
    private int userId;
    private String roleName;
}
