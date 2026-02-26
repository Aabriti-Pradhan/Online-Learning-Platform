package com.finalyearproject.fyp.dao;

import com.finalyearproject.fyp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDAO extends JpaRepository<User, Long> {
    User findByEmail(String email);
}
