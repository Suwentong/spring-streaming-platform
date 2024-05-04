package com.company.streamingplatform.mapper;

import com.company.streamingplatform.dto.request.MovieUploadRequest;
import com.company.streamingplatform.movie.Movie;
import org.springframework.stereotype.Component;

@Component
public class MovieMapper {

    public Movie convertToMovieEntity(MovieUploadRequest request) {
        Movie movie = new Movie();
        movie.setTitle(request.getTitle());
        movie.setDescription(request.getDescription());
        movie.setRelease_date(request.getRelease_date());
        movie.setDuration_minutes(request.getDuration_minutes());
        movie.setLanguage(request.getLanguage());
        movie.setCountry(request.getCountry());
        movie.setDirector(request.getDirector());
        movie.setGenre(request.getGenre());
        movie.setRating(request.getRating());
        movie.setCover_image_url(request.getCover_image_url());
        movie.setCreated_at(request.getCreated_at());
        movie.setUpdated_at(request.getUpdated_at());
        return movie;
    }
}
