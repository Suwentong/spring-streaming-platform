package com.company.streamingplatform.movie;

import com.company.streamingplatform.aws.AmazonService;
import com.company.streamingplatform.dto.request.MovieUploadRequest;
import com.company.streamingplatform.kafka.producer.TranscodingProducer;
import com.company.streamingplatform.mapper.MovieMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;
    private final MovieMapper movieMapper;
    private final AmazonService amazonClient;
    private final TranscodingProducer transcodingProducer;

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

    public String addMovie(MovieUploadRequest request, MultipartFile file) {
        Movie movie = movieMapper.convertToMovieEntity(request);
        Movie savedMovie = movieRepository.save(movie);
        Long movieId = Long.valueOf(savedMovie.getId());
        String response = amazonClient.uploadFile(file);  // Upload file to S3
        transcodingProducer.sendMessage(response + "," + movieId);   // Send message to transcoding topic
        return response;
    }

    public void updateMovieUrl(Long id, Map<String, String> newMovieUrl) {
        Movie movie = movieRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Movie not found"));
        Map<String, String> movieUrl = movie.getMovie_url();
        if (movieUrl == null) {
            movieUrl = newMovieUrl;
        } else {
            movieUrl.putAll(newMovieUrl);
        }
        movie.setMovie_url(movieUrl);
        movieRepository.save(movie);
    }
}
