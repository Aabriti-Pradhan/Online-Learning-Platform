package com.finalyearproject.fyp.service.serviceImpl;

import com.finalyearproject.fyp.dto.RegisterDTO;
import com.finalyearproject.fyp.entity.User;
import com.finalyearproject.fyp.dao.UserDAO;
import com.finalyearproject.fyp.service.UserService;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserDAO userDao;

    public UserServiceImpl(UserDAO userDao) {
        this.userDao = userDao;
    }

    @Override
    public User registerUser(RegisterDTO dto) {

        User user = new User();
        user.setUsername(dto.username());
        user.setEmail(dto.email());
        user.setRole(dto.role());

        String hashedPassword = BCrypt.hashpw(dto.password(), BCrypt.gensalt());
        user.setPassword(hashedPassword);

        return userDao.save(user);
    }

    @Override
    public User loginUser(String email, String password) {
        User user = userDao.findByEmail(email);

        if (user == null) {
            throw new RuntimeException("Invalid email or password");
        }

        if (!BCrypt.checkpw(password, user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        return user;
    }

    @Override
    public boolean isEmailTaken(String email) {
        return userDao.findByEmail(email) != null;
    }

    @Override
    public User findByEmail(String email) {
        return userDao.findByEmail(email);
    }

    @Override
    public User registerOAuthUser(String email, String name, String role) {
        User existing = userDao.findByEmail(email);
        if (existing != null) return existing;
        User user = new User();
        user.setEmail(email);
        user.setUsername(name);
        user.setRole(role);
        return userDao.save(user);
    }

    @Override
    public boolean checkPassword(String rawPassword, String hashedPassword) {
        return BCrypt.checkpw(rawPassword, hashedPassword);
    }

    @Override
    public User updateProfile(String email, String newUsername, String newPassword, MultipartFile profilePicture) {
        User user = userDao.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        if (newUsername != null && !newUsername.isBlank()) {
            user.setUsername(newUsername);
        }
        if (newPassword != null && !newPassword.isBlank()) {
            user.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
        }
        if (profilePicture != null && !profilePicture.isEmpty()) {
            try {
                String ext = "";
                String original = profilePicture.getOriginalFilename();
                if (original != null && original.contains(".")) {
                    ext = original.substring(original.lastIndexOf("."));
                }
                String filename = "user_" + user.getUserId() + "_" + UUID.randomUUID().toString().substring(0, 8) + ext;
                Path dir = Paths.get("uploads", "profile-pics");
                Files.createDirectories(dir);
                Path dest = dir.resolve(filename);
                Files.copy(profilePicture.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);
                user.setProfilePicture("/files/profile-pics/" + filename);
            } catch (IOException e) {
                throw new RuntimeException("Failed to save profile picture: " + e.getMessage());
            }
        }
        return userDao.save(user);
    }
}