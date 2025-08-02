package com.alibou.security.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileRequest {
    private String fullName;
    private String phoneNumber;
    private String address;
    private String city;
    private String country;
    private String postalCode;
    private LocalDate dateOfBirth;
    private String bio;
    private String profileImageUrl;
    private UserProfile.ActivityStatus activityStatus;
}
