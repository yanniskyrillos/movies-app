package com.studio.movierama.service;

import com.studio.movierama.config.security.MovieRamaUserDetails;
import com.studio.movierama.domain.Movie;
import com.studio.movierama.domain.Rating;
import com.studio.movierama.domain.RatingId;
import com.studio.movierama.dto.MovieDto;
import com.studio.movierama.dto.MovieRatingRequestDto;
import com.studio.movierama.exception.MovieRamaException;
import com.studio.movierama.repository.MovieRepository;
import com.studio.movierama.repository.UserMovieRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MovieService {

    private MovieRepository movieRepository;
    private UserMovieRepository userMovieRepository;
    private ConversionService conversionService;

    @Autowired
    public MovieService(MovieRepository movieRepository, ConversionService conversionService,
                        UserMovieRepository userMovieRepository) {
        this.movieRepository = movieRepository;
        this.conversionService = conversionService;
        this.userMovieRepository = userMovieRepository;
    }

    public MovieDto save(MovieDto movieDto) {
        log.info("saving movie");
        Movie movie = conversionService.convert(movieDto, Movie.class);
        Instant now = Instant.now();
        movie.setPublicationDate(now);
        movieRepository.save(movie);
        movieDto = conversionService.convert(movie, MovieDto.class);
        return movieDto;
    }

    public Page<MovieDto> findAll(Pageable pageable) {
        log.info("finding all movies");
        String username = null;
        Long userId = null;
        MovieRamaUserDetails loggedInUser = getLoggedInUserDetails();
        if (loggedInUser != null) {
            username = loggedInUser.getUsername();
            userId = loggedInUser.getId();
        }
        Page<Movie> movies = movieRepository.findAll(pageable);
        List<MovieDto> movieDtoList = movies
                .stream()
                .map(movie -> conversionService.convert(movie, MovieDto.class))
                .collect(Collectors.toList());
        if (username != null) {
            movieDtoList = setLoggedInUserRatings(userId, movieDtoList);
        }
        Page<MovieDto> movieDtoPage = new PageImpl<>(movieDtoList, pageable, movies.getTotalElements());
        return movieDtoPage;
    }

    private MovieRamaUserDetails getLoggedInUserDetails() {
        Object principal = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        if (principal instanceof MovieRamaUserDetails) {
            return (MovieRamaUserDetails) principal;
        }
        return null;
    }

    private List<MovieDto> setLoggedInUserRatings(Long loggedInUserId, List<MovieDto> movies) {
        List<RatingId> ratingIds = movies
                .stream()
                .map(movieDto -> new RatingId(loggedInUserId, movieDto.getId()))
                .toList();
        List<Rating> ratings = userMovieRepository.findAllById(ratingIds);
        return movies.stream()
                .map(movieDto -> {
                    Rating rating = ratings.stream()
                            .filter(rating1 -> rating1.getRatingId().getMovieId().equals(movieDto.getId()))
                            .findFirst()
                            .orElse(null);
                    if (rating != null) {
                        if (com.studio.movierama.enums.Rating.LIKE.getBooleanValue() == rating.isLiked()) {
                            movieDto.setLikedByUser(true);
                        } else if (com.studio.movierama.enums.Rating.DISLIKE.getBooleanValue() == rating.isLiked()) {
                            movieDto.setHatedByUser(false);
                        }
                    }
                    return movieDto;
                })
                .toList();
    }

    public void rate(MovieRatingRequestDto movieRatingRequestDto) {
        if (movieRatingRequestDto.isRetract()) {
            retractRating(movieRatingRequestDto.getMovieId(), movieRatingRequestDto.getUserId());
            return;
        }
        switch (movieRatingRequestDto.getRating()) {
            case LIKE:
                like(movieRatingRequestDto.getMovieId(), movieRatingRequestDto.getUserId());
                break;
            case DISLIKE:
                dislike(movieRatingRequestDto.getMovieId(), movieRatingRequestDto.getUserId());
                break;
        }
    }

    private void dislike(Long movieId, Long userId) {
        log.info("Mark movie with id: {} hated by user with id: {}", movieId, userId);
        rate(movieId, userId, com.studio.movierama.enums.Rating.DISLIKE);
    }

    private void like(Long movieId, Long userId) {
        log.info("Mark movie with id: {} liked by user with id: {}", movieId, userId);
        rate(movieId, userId, com.studio.movierama.enums.Rating.LIKE);
    }

    private void retractRating(Long movieId, Long userId) {
        log.info("retracting rating of user: {} for movie: {}", userId, movieId);
        userMovieRepository.deleteById(new RatingId(userId, movieId));
    }

    private void rate(Long movieId, Long userId, com.studio.movierama.enums.Rating likeHateFlag) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new MovieRamaException("Movie not found"));
        if (movie.getSubmitter().getId().equals(userId)) {
            throw new MovieRamaException("User cannot like their own movie");
        }
        Rating rating = userMovieRepository.findById(new RatingId(userId, movieId))
                .orElse(Rating
                        .builder()
                        .ratingId(new RatingId(userId, movieId))
                        .build());
        if (likeHateFlag.getBooleanValue() == rating.isLiked()) {
            return;
        }
        rating.setLiked(likeHateFlag.getBooleanValue());
        userMovieRepository.save(rating);
    }
}
