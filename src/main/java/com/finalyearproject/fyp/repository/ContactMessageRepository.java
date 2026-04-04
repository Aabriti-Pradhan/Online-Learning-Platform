package com.finalyearproject.fyp.repository;

import com.finalyearproject.fyp.entity.ContactMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {
    List<ContactMessage> findAllByOrderBySentAtDesc();
    long countByIsReadFalse();
}
