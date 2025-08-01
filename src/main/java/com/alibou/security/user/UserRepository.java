package com.alibou.security.user;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
  Optional<User> findByEmail(String email);
  Optional<User> findByUsername(String username);
  Page<User> findByEmailContainingOrFirstnameContainingOrLastnameContaining(String email, String firstname, String lastname, Pageable pageable);
}
