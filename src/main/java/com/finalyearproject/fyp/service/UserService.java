package com.finalyearproject.fyp.service;

import com.finalyearproject.fyp.dto.RegisterDTO;
import com.finalyearproject.fyp.entity.User;

public interface UserService {
    User registerUser(RegisterDTO dto);
    User loginUser(String email, String password);
    boolean isEmailTaken(String email);
    User findByEmail(String email);
    boolean checkPassword(String rawPassword, String hashedPassword);
}
