package com.company.streamingplatform.dto.request;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MovieUploadRequest {

    private String title;
    private String description;
    private String release_date;
    private Integer duration_minutes;
    private String language;
    private String country;
    private String director;
    private String genre;
    private Double rating;
    private String cover_image_url;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;
}
