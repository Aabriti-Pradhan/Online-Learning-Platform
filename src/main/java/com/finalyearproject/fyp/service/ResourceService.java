package com.finalyearproject.fyp.service;

import com.finalyearproject.fyp.entity.Resource;

import java.util.List;

public interface ResourceService {
    List<Resource> getUserResources(String email);
    void savePdf(Long userId, Long courseId,
                 String fileId,
                 String originalFileName);
}
