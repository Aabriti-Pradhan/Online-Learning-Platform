package com.finalyearproject.fyp.repository;

import com.finalyearproject.fyp.entity.Resource;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {
    @Modifying
    @Transactional
    @Query("DELETE FROM Resource r WHERE r.resourceId IN :resourceIds")
    void deleteByResourceIds(@Param("resourceIds") Set<Long> resourceIds);
}
