package com.finalyearproject.fyp.repository;

import com.finalyearproject.fyp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByRole(String role);

    long countByRole(String role);

    @Query(value = """
        SELECT * FROM users u
        WHERE (:role IS NULL OR u.role = :role)
          AND (
            :search IS NULL
            OR LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(u.email)    LIKE LOWER(CONCAT('%', :search, '%'))
          )
        ORDER BY u.user_id ASC
        """, nativeQuery = true)
    List<User> searchUsers(@Param("role") String role, @Param("search") String search);
}