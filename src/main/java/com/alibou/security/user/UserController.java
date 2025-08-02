package com.alibou.security.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService service;

    @PatchMapping
    public ResponseEntity<?> changePassword(
          @RequestBody ChangePasswordRequest request,
          Principal connectedUser
    ) {
        service.changePassword(request, connectedUser);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<Page<User>> getUsers(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("GET /users - User: {}, Authorities: {}", 
                 auth.getName(), auth.getAuthorities());
        PageRequest pageRequest = PageRequest.of(page, size);
        return ResponseEntity.ok(service.getUsers(search, pageRequest));
    }

    @PutMapping("/role")
    public ResponseEntity<String> updateUserRole(@RequestBody UpdateUserRoleRequest request) {
        service.updateUserRole(request.getUserId(), request.getRoleName());
        return ResponseEntity.ok("Cập nhật vai trò thành công");
    }

    @PatchMapping("/lock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> setUserLock(@RequestBody LockUserRequest request) {
        service.setUserLock(request.getUserId(), request.isLock());
        return ResponseEntity.ok(request.isLock() ? "Khóa tài khoản thành công" : "Mở khóa tài khoản thành công");
    }
}
