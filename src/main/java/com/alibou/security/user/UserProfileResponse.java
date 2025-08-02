package com.alibou.security.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private Integer id;
    private String fullName;
    private String phoneNumber;
    private String address;
    private String city;
    private String country;
    private String postalCode;
    private LocalDate dateOfBirth;
    private UserProfile.ActivityStatus activityStatus;
    private String activityStatusDescription;
    private String bio;
    private String profileImageUrl;
    private Boolean isEmailVerified;
    private Boolean isPhoneVerified;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    
    // User basic info
    private String email;
    private String username;
    private Role role;
}
