package com.alibou.security.user;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/profiles")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService profileService;

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getCurrentUserProfile(Principal connectedUser) {
        return ResponseEntity.ok(profileService.getCurrentUserProfile(connectedUser));
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateCurrentUserProfile(
            @RequestBody UserProfileRequest request,
            Principal connectedUser) {
        return ResponseEntity.ok(profileService.updateCurrentUserProfile(request, connectedUser));
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EDITOR')")
    public ResponseEntity<UserProfileResponse> getUserProfile(@PathVariable Integer userId) {
        return ResponseEntity.ok(profileService.getUserProfile(userId));
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserProfileResponse> updateUserProfile(
            @PathVariable Integer userId,
            @RequestBody UserProfileRequest request) {
        return ResponseEntity.ok(profileService.updateUserProfile(userId, request));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('EDITOR')")
    public ResponseEntity<Page<UserProfileResponse>> getAllProfiles(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return ResponseEntity.ok(profileService.getAllProfiles(search, pageRequest));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EDITOR')")
    public ResponseEntity<Page<UserProfileResponse>> getProfilesByStatus(
            @PathVariable UserProfile.ActivityStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return ResponseEntity.ok(profileService.getProfilesByStatus(status, pageRequest));
    }

    @PatchMapping("/{userId}/activity-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> updateActivityStatus(
            @PathVariable Integer userId,
            @RequestBody Map<String, String> request) {
        UserProfile.ActivityStatus status = UserProfile.ActivityStatus.valueOf(request.get("status"));
        profileService.updateActivityStatus(userId, status);
        return ResponseEntity.ok("Activity status updated successfully");
    }

    @PatchMapping("/{userId}/verify-email")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> verifyEmail(@PathVariable Integer userId) {
        profileService.verifyEmail(userId);
        return ResponseEntity.ok("Email verified successfully");
    }

    @PatchMapping("/{userId}/verify-phone")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> verifyPhone(@PathVariable Integer userId) {
        profileService.verifyPhone(userId);
        return ResponseEntity.ok("Phone verified successfully");
    }

    @GetMapping("/activity-statuses")
    public ResponseEntity<UserProfile.ActivityStatus[]> getActivityStatuses() {
        return ResponseEntity.ok(UserProfile.ActivityStatus.values());
    }
}
