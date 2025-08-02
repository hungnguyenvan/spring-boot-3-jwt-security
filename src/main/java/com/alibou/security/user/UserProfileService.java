package com.alibou.security.user;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;

@Service
@RequiredArgsConstructor
@Transactional
public class UserProfileService {

    private final UserProfileRepository profileRepository;
    private final UserRepository userRepository;

    public UserProfileResponse getCurrentUserProfile(Principal connectedUser) {
        var user = (User) ((org.springframework.security.authentication.UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();
        return getUserProfile(user.getId());
    }

    public UserProfileResponse getUserProfile(Integer userId) {
        var profile = profileRepository.findByUserId(userId)
                .orElse(null);
        
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        
        return UserProfileResponse.builder()
                .id(profile != null ? profile.getId() : null)
                .fullName(profile != null ? profile.getFullName() : null)
                .phoneNumber(profile != null ? profile.getPhoneNumber() : null)
                .address(profile != null ? profile.getAddress() : null)
                .city(profile != null ? profile.getCity() : null)
                .country(profile != null ? profile.getCountry() : null)
                .postalCode(profile != null ? profile.getPostalCode() : null)
                .dateOfBirth(profile != null ? profile.getDateOfBirth() : null)
                .activityStatus(profile != null ? profile.getActivityStatus() : UserProfile.ActivityStatus.ACTIVE)
                .activityStatusDescription(profile != null ? profile.getActivityStatus().getDescription() : UserProfile.ActivityStatus.ACTIVE.getDescription())
                .bio(profile != null ? profile.getBio() : null)
                .profileImageUrl(profile != null ? profile.getProfileImageUrl() : null)
                .isEmailVerified(profile != null ? profile.getIsEmailVerified() : false)
                .isPhoneVerified(profile != null ? profile.getIsPhoneVerified() : false)
                .createdDate(profile != null ? profile.getCreatedDate() : null)
                .lastModifiedDate(profile != null ? profile.getLastModifiedDate() : null)
                .email(user.getEmail())
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }

    public UserProfileResponse updateCurrentUserProfile(UserProfileRequest request, Principal connectedUser) {
        var user = (User) ((org.springframework.security.authentication.UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();
        return updateUserProfile(user.getId(), request);
    }

    public UserProfileResponse updateUserProfile(Integer userId, UserProfileRequest request) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        var profile = profileRepository.findByUserId(userId)
                .orElse(UserProfile.builder()
                        .user(user)
                        .activityStatus(UserProfile.ActivityStatus.ACTIVE)
                        .isEmailVerified(false)
                        .isPhoneVerified(false)
                        .build());

        // Update profile fields
        if (request.getFullName() != null) {
            profile.setFullName(request.getFullName());
        }
        if (request.getPhoneNumber() != null) {
            profile.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getAddress() != null) {
            profile.setAddress(request.getAddress());
        }
        if (request.getCity() != null) {
            profile.setCity(request.getCity());
        }
        if (request.getCountry() != null) {
            profile.setCountry(request.getCountry());
        }
        if (request.getPostalCode() != null) {
            profile.setPostalCode(request.getPostalCode());
        }
        if (request.getDateOfBirth() != null) {
            profile.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getBio() != null) {
            profile.setBio(request.getBio());
        }
        if (request.getProfileImageUrl() != null) {
            profile.setProfileImageUrl(request.getProfileImageUrl());
        }
        if (request.getActivityStatus() != null) {
            profile.setActivityStatus(request.getActivityStatus());
        }

        var savedProfile = profileRepository.save(profile);
        return getUserProfile(userId);
    }

    public Page<UserProfileResponse> getAllProfiles(String search, Pageable pageable) {
        Page<UserProfile> profiles;
        if (search != null && !search.trim().isEmpty()) {
            profiles = profileRepository.findBySearchTerm(search, pageable);
        } else {
            profiles = profileRepository.findAll(pageable);
        }
        
        return profiles.map(this::mapToResponse);
    }

    public Page<UserProfileResponse> getProfilesByStatus(UserProfile.ActivityStatus status, Pageable pageable) {
        return profileRepository.findByActivityStatus(status, pageable)
                .map(this::mapToResponse);
    }

    public void updateActivityStatus(Integer userId, UserProfile.ActivityStatus status) {
        var profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("User profile not found"));
        
        profile.setActivityStatus(status);
        profileRepository.save(profile);
    }

    public void verifyEmail(Integer userId) {
        var profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("User profile not found"));
        
        profile.setIsEmailVerified(true);
        profileRepository.save(profile);
    }

    public void verifyPhone(Integer userId) {
        var profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("User profile not found"));
        
        profile.setIsPhoneVerified(true);
        profileRepository.save(profile);
    }

    private UserProfileResponse mapToResponse(UserProfile profile) {
        return UserProfileResponse.builder()
                .id(profile.getId())
                .fullName(profile.getFullName())
                .phoneNumber(profile.getPhoneNumber())
                .address(profile.getAddress())
                .city(profile.getCity())
                .country(profile.getCountry())
                .postalCode(profile.getPostalCode())
                .dateOfBirth(profile.getDateOfBirth())
                .activityStatus(profile.getActivityStatus())
                .activityStatusDescription(profile.getActivityStatus().getDescription())
                .bio(profile.getBio())
                .profileImageUrl(profile.getProfileImageUrl())
                .isEmailVerified(profile.getIsEmailVerified())
                .isPhoneVerified(profile.getIsPhoneVerified())
                .createdDate(profile.getCreatedDate())
                .lastModifiedDate(profile.getLastModifiedDate())
                .email(profile.getUser().getEmail())
                .username(profile.getUser().getUsername())
                .role(profile.getUser().getRole())
                .build();
    }
}
