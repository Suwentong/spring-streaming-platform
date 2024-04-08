package com.company.streamingplatform.movie;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class MovieService {

    private final MovieRepository movieRepository;

    @Autowired
    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    public ResponseEntity<List<Movie>> getAllMovies() {
        List<Movie> movieList = movieRepository.findAllByOrderByIdAsc().orElse(null);
        if (movieList == null) {
            throw new IllegalStateException("An error has occurred. Please try again later.");
        }
        return ResponseEntity.ok(movieList);
    }

    public ResponseEntity<Movie> getMovieById(Long id) {
        Movie movie = movieRepository.findById(id).orElse(null);
        if (movie == null) {
            throw new EntityNotFoundException("Movie not found");
        }
        return ResponseEntity.ok(movie);
    }

    public ResponseEntity<List<Movie>> searchMovie(String title) {
        if (title != null && !title.isEmpty()) {
            return ResponseEntity.ok(movieRepository.findByTitleContainingIgnoreCase(title).orElse(null));
        } else {
            return ResponseEntity.ok(movieRepository.findAll());
        }
    }
}
