package com.finalyearproject.fyp.repository;

import com.finalyearproject.fyp.entity.Test;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface TestRepository extends JpaRepository<Test, Long> {
    @Modifying
    @Transactional
    @Query("DELETE FROM Test t WHERE t.testId IN :testIds")
    void deleteByTestIds(@Param("testIds") Set<Long> testIds);
}