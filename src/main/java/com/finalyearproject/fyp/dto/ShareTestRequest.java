package com.finalyearproject.fyp.dto;

import java.util.List;

public record ShareTestRequest(
        Long       testId,
        Long       courseId,
        List<Long> friendIds   // share with one or more friends at once
) {}