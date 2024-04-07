package com.company.streamingplatform.movie;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "movies")
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
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
    private String trailer_url;
    private String movie_url;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;
}
