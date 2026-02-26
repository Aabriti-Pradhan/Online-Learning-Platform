package com.finalyearproject.fyp.service.serviceImpl;

import com.finalyearproject.fyp.dto.RegisterDTO;
import com.finalyearproject.fyp.entity.User;
import com.finalyearproject.fyp.dao.UserDAO;
import com.finalyearproject.fyp.service.UserService;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

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
    public boolean checkPassword(String rawPassword, String hashedPassword) {
        return BCrypt.checkpw(rawPassword, hashedPassword);
    }

}
