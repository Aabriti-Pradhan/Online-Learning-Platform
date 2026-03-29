package com.finalyearproject.fyp.dto;

public record ActivityDayDTO(
        String date,   // yyyy-MM-dd
        int    count   // number of resources accessed + notes written
) {}