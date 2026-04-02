package com.finalyearproject.fyp.repository;

import com.finalyearproject.fyp.entity.Enrollment;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    @Modifying
    @Transactional
    @Query("DELETE FROM Enrollment e WHERE e.enrollmentId IN :enrollmentIds")
    void deleteByEnrollmentIds(@Param("enrollmentIds") Set<Long> enrollmentIds);
}