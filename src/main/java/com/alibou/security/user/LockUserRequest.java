package com.alibou.security.user;

import lombok.Data;

@Data
public class LockUserRequest {
    private int userId;
    private boolean lock;
}
