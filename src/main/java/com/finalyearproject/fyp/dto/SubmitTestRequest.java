package com.finalyearproject.fyp.dto;

import java.util.Map;

public record SubmitTestRequest(
        Map<String, String> answers
) {}