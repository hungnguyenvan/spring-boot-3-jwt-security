package com.alibou.security.auth;

import com.alibou.security.config.JwtService;
import com.alibou.security.token.Token;
import com.alibou.security.token.TokenRepository;
import com.alibou.security.token.TokenType;
import com.alibou.security.user.User;
import com.alibou.security.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
  private final UserRepository repository;
  private final TokenRepository tokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;

  public AuthenticationResponse register(RegisterRequest request) {
    // Validation để chống spam registration
    validateRegistrationRequest(request);

    // Kiểm tra trùng email
    if (repository.findByEmail(request.getEmail()).isPresent()) {
      throw new IllegalStateException("Email đã tồn tại!");
    }
    // Kiểm tra trùng username
    if (repository.findByUsername(request.getUsername()).isPresent()) {
      throw new IllegalStateException("Username đã tồn tại!");
    }
    var user = User.builder()
        .firstname(request.getFirstname())
        .lastname(request.getLastname())
        .email(request.getEmail())
        .username(request.getUsername())
        .password(passwordEncoder.encode(request.getPassword()))
        .role(request.getRole())
        .build();
    var savedUser = repository.save(user);
    var jwtToken = jwtService.generateToken(user);
    var refreshToken = jwtService.generateRefreshToken(user);
    saveUserToken(savedUser, jwtToken);
    return AuthenticationResponse.builder()
        .accessToken(jwtToken)
            .refreshToken(refreshToken)
        .build();
  }

  public AuthenticationResponse authenticate(AuthenticationRequest request) {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            request.getEmail(),
            request.getPassword()
        )
    );
    var user = repository.findByEmail(request.getEmail())
        .orElseThrow();
    var jwtToken = jwtService.generateToken(user);
    var refreshToken = jwtService.generateRefreshToken(user);
    revokeAllUserTokens(user);
    saveUserToken(user, jwtToken);
    return AuthenticationResponse.builder()
        .accessToken(jwtToken)
            .refreshToken(refreshToken)
        .build();
  }

  private void saveUserToken(User user, String jwtToken) {
    var token = Token.builder()
        .user(user)
        .token(jwtToken)
        .tokenType(TokenType.BEARER)
        .expired(false)
        .revoked(false)
        .build();
    tokenRepository.save(token);
  }

  private void revokeAllUserTokens(User user) {
    var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
    if (validUserTokens.isEmpty())
      return;
    validUserTokens.forEach(token -> {
      token.setExpired(true);
      token.setRevoked(true);
    });
    tokenRepository.saveAll(validUserTokens);
  }

  public void refreshToken(
          HttpServletRequest request,
          HttpServletResponse response
  ) throws IOException {
    final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
    final String refreshToken;
    final String userEmail;
    if (authHeader == null ||!authHeader.startsWith("Bearer ")) {
      return;
    }
    refreshToken = authHeader.substring(7);
    userEmail = jwtService.extractUsername(refreshToken);
    if (userEmail != null) {
      var user = this.repository.findByEmail(userEmail)
              .orElseThrow();
      if (jwtService.isTokenValid(refreshToken, user)) {
        // Revoke refresh token ngay sau khi sử dụng
        tokenRepository.findByToken(refreshToken).ifPresent(token -> {
          token.setExpired(true);
          token.setRevoked(true);
          tokenRepository.save(token);
        });
        var accessToken = jwtService.generateToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, accessToken);
        var authResponse = AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
        new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
      }
    }
  }

  public void resetPassword(String email, String newPassword) {
    var user = repository.findByEmail(email)
        .orElseThrow(() -> new IllegalStateException("User not found"));
    user.setPassword(passwordEncoder.encode(newPassword));
    repository.save(user);
  }

  public void deleteUserByEmail(String email) {
    var user = repository.findByEmail(email)
        .orElseThrow(() -> new IllegalStateException("User not found"));
    repository.delete(user);
  }

  // Validation để chống spam registration
  private void validateRegistrationRequest(RegisterRequest request) {
      if (request.getEmail() == null || !request.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
          throw new IllegalArgumentException("Invalid email format");
      }
      if (request.getUsername() == null || request.getUsername().length() < 3 || request.getUsername().length() > 20) {
          throw new IllegalArgumentException("Username must be between 3-20 characters");
      }
      if (request.getPassword() == null || request.getPassword().length() < 6) {
          throw new IllegalArgumentException("Password must be at least 6 characters");
      }
      if (request.getFirstname() == null || request.getFirstname().trim().isEmpty()) {
          throw new IllegalArgumentException("First name is required");
      }
      if (request.getLastname() == null || request.getLastname().trim().isEmpty()) {
          throw new IllegalArgumentException("Last name is required");
      }
  }
}
