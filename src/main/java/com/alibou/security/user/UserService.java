package com.alibou.security.user;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Service
@RequiredArgsConstructor
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository repository;
    public void changePassword(ChangePasswordRequest request, Principal connectedUser) {

        var user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();

        // check if the current password is correct
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalStateException("Wrong password");
        }
        // check if the two new passwords are the same
        if (!request.getNewPassword().equals(request.getConfirmationPassword())) {
            throw new IllegalStateException("Password are not the same");
        }

        // update the password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        // save the new password
        repository.save(user);
    }

    public Page<User> getUsers(String search, Pageable pageable) {
        return repository.findByEmailContainingOrFirstnameContainingOrLastnameContaining(search, search, search, pageable);
    }

    public void updateUserRole(int userId, String roleName) {
        var user = repository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        user.setRole(Role.valueOf(roleName));
        repository.save(user);
    }

    public void setUserLock(int userId, boolean lock) {
        var user = repository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        user.setLocked(lock);
        repository.save(user);
    }
}
