package com.finalyearproject.fyp.repository;

import com.finalyearproject.fyp.entity.Course;
import com.finalyearproject.fyp.entity.SharedTest;
import com.finalyearproject.fyp.entity.Test;
import com.finalyearproject.fyp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface SharedTestRepository extends JpaRepository<SharedTest, Long> {

    @Query("SELECT st FROM SharedTest st WHERE st.sharedTo = :user ORDER BY st.sharedAt DESC")
    List<SharedTest> findBySharedTo(@Param("user") User user);

    @Query("SELECT st FROM SharedTest st WHERE st.sharedBy = :user ORDER BY st.sharedAt DESC")
    List<SharedTest> findBySharedBy(@Param("user") User user);

    @Query("SELECT st FROM SharedTest st WHERE st.test = :test AND st.sharedBy = :sharedBy AND st.sharedTo = :sharedTo")
    Optional<SharedTest> findByTestAndSharedByAndSharedTo(
            @Param("test") Test test,
            @Param("sharedBy") User sharedBy,
            @Param("sharedTo") User sharedTo);

    @Query("SELECT st FROM SharedTest st WHERE st.test = :test")
    List<SharedTest> findByTest(@Param("test") Test test);

    @Query("SELECT st FROM SharedTest st WHERE st.course = :course")
    List<SharedTest> findByCourse(@Param("course") Course course);

    @Modifying
    @Transactional
    @Query("DELETE FROM SharedTest st WHERE st.course = :course")
    void deleteByCourse(@Param("course") Course course);
}