package com.alibou.security.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthenticationController {

  private final AuthenticationService service;

  @PostMapping(value = "/register", 
               consumes = "application/json", 
               produces = "application/json")
  public ResponseEntity<AuthenticationResponse> register(
      @RequestBody RegisterRequest request
  ) {
    return ResponseEntity.ok(service.register(request));
  }
  
  @PostMapping(value = "/authenticate", 
               consumes = "application/json", 
               produces = "application/json")
  public ResponseEntity<AuthenticationResponse> authenticate(
      @RequestBody AuthenticationRequest request
  ) {
    return ResponseEntity.ok(service.authenticate(request));
  }

  @PostMapping("/refresh-token")
  public void refreshToken(
      HttpServletRequest request,
      HttpServletResponse response
  ) throws IOException {
    service.refreshToken(request, response);
  }

  @PostMapping(value = "/reset-password", 
               consumes = "application/json", 
               produces = "application/json")
  public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {
    // Debug authorities ngay ở đây
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    log.info("Reset password - User: {}, Authorities: {}", 
             auth != null ? auth.getName() : "null", 
             auth != null ? auth.getAuthorities() : "null");
    
    service.resetPassword(request.getEmail(), request.getNewPassword());
    return ResponseEntity.ok("Password reset successful");
  }

  @PostMapping(value = "/delete-user", 
               consumes = "application/json", 
               produces = "application/json")
  @PreAuthorize("hasRole('ADMIN')") 
  public ResponseEntity<String> deleteUser(@RequestBody DeleteUserRequest request) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    log.info("Delete user request - Current user: {}, Authorities: {}", 
             auth.getName(), auth.getAuthorities());
    
    // Manual check thay vì @PreAuthorize
    boolean isAdmin = auth.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    
    if (!isAdmin) {
        // Return authorities trong error message để debug
        return ResponseEntity.status(403).body(
            "Access denied - ADMIN role required. Current authorities: " + auth.getAuthorities()
        );
    }
    
    service.deleteUserByEmail(request.getEmail());
    return ResponseEntity.ok("User deleted successfully");
  }

  @GetMapping("/debug-authorities")
  public ResponseEntity<?> debugAuthorities() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    return ResponseEntity.ok(Map.of(
        "user", auth.getName(),
        "authorities", auth.getAuthorities(),
        "isAdmin", auth.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))
    ));
  }


}
