package com.company.streamingplatform.movie;

import com.company.streamingplatform.dto.request.MovieUploadRequest;
import com.company.streamingplatform.dto.response.MovieUploadResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/movies")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Movie")
public class MovieController {

    private final MovieService movieService;

    @GetMapping
    public ResponseEntity<List<Movie>> getAllMovies() {
        return movieService.getAllMovies();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Movie> getMovieById(@PathVariable Long id) {
        return movieService.getMovieById(id);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Movie>> searchMovie(@RequestParam(required = false) String title) {
        return movieService.searchMovie(title);
    }

    @PostMapping
    public ResponseEntity<MovieUploadResponse> addMovie(
            @RequestPart(value = "movie") MovieUploadRequest request,
            @RequestPart(value = "file") MultipartFile file) {
        String response = movieService.addMovie(request, file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(MovieUploadResponse.builder()
                        .message("Movie uploaded successfully")
                        .fileName(response)
                        .build());
    }
}
