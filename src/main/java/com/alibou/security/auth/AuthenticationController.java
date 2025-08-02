package com.alibou.security.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
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
    service.resetPassword(request.getEmail(), request.getNewPassword());
    return ResponseEntity.ok("Password reset successful");
  }

  @PostMapping(value = "/delete-user", 
               consumes = "application/json", 
               produces = "application/json")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<String> deleteUser(@RequestBody DeleteUserRequest request) {
    service.deleteUserByEmail(request.getEmail());
    return ResponseEntity.ok("User deleted successfully");
  }


}
