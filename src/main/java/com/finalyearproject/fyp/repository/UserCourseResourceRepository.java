package com.finalyearproject.fyp.repository;

import com.finalyearproject.fyp.entity.User;
import com.finalyearproject.fyp.entity.UserCourseResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserCourseResourceRepository extends JpaRepository<UserCourseResource, Long>
    {
        List<UserCourseResource> findByUser(User user);
    }
