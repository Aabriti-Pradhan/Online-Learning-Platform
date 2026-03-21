package com.finalyearproject.fyp.repository;

import com.finalyearproject.fyp.entity.SharedTest;
import com.finalyearproject.fyp.entity.Test;
import com.finalyearproject.fyp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SharedTestRepository extends JpaRepository<SharedTest, Long> {

    // All tests shared TO a specific user
    @Query("SELECT st FROM SharedTest st WHERE st.sharedTo = :user ORDER BY st.sharedAt DESC")
    List<SharedTest> findBySharedTo(@Param("user") User user);

    // All tests shared BY a specific user
    @Query("SELECT st FROM SharedTest st WHERE st.sharedBy = :user ORDER BY st.sharedAt DESC")
    List<SharedTest> findBySharedBy(@Param("user") User user);

    // Check if already shared (prevent duplicates)
    @Query("SELECT st FROM SharedTest st WHERE st.test = :test AND st.sharedBy = :sharedBy AND st.sharedTo = :sharedTo")
    Optional<SharedTest> findByTestAndSharedByAndSharedTo(
            @Param("test") Test test,
            @Param("sharedBy") User sharedBy,
            @Param("sharedTo") User sharedTo);

    // All shares for a specific test (so teacher can see who shared it)
    @Query("SELECT st FROM SharedTest st WHERE st.test = :test")
    List<SharedTest> findByTest(@Param("test") Test test);
}