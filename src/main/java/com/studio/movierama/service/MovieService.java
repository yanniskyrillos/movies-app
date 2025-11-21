package com.studio.movierama.service;

import com.studio.movierama.config.security.MovieRamaUserDetails;
import com.studio.movierama.domain.Movie;
import com.studio.movierama.domain.Opinion;
import com.studio.movierama.domain.OpinionId;
import com.studio.movierama.dto.MovieDto;
import com.studio.movierama.dto.MovieRatingRequestDto;
import com.studio.movierama.enums.Rating;
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
        List<OpinionId> opinionIds = movies
                .stream()
                .map(movieDto -> new OpinionId(loggedInUserId, movieDto.getId()))
                .toList();
        List<Opinion> opinions = userMovieRepository.findAllById(opinionIds);
        return movies.stream()
                .map(movieDto -> {
                    Opinion opinion = opinions.stream()
                            .filter(opinion1 -> opinion1.getOpinionId().getMovieId().equals(movieDto.getId()))
                            .findFirst()
                            .orElse(null);
                    if (opinion != null) {
                        if (Rating.LIKE.getBooleanValue() == opinion.isLiked()) {
                            movieDto.setLikedByUser(true);
                        } else if (Rating.DISLIKE.getBooleanValue() == opinion.isLiked()) {
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
        rate(movieId, userId, Rating.DISLIKE);
    }

    private void like(Long movieId, Long userId) {
        log.info("Mark movie with id: {} liked by user with id: {}", movieId, userId);
        rate(movieId, userId, Rating.LIKE);
    }

    private void retractRating(Long movieId, Long userId) {
        log.info("retracting rating of user: {} for movie: {}", userId, movieId);
        userMovieRepository.deleteById(new OpinionId(userId, movieId));
    }

    private void rate(Long movieId, Long userId, Rating likeHateFlag) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new MovieRamaException("Movie not found"));
        if (movie.getSubmitter().getId().equals(userId)) {
            throw new MovieRamaException("User cannot like their own movie");
        }
        Opinion opinion = userMovieRepository.findById(new OpinionId(userId, movieId))
                .orElse(Opinion
                        .builder()
                        .opinionId(new OpinionId(userId, movieId))
                        .build());
        if (likeHateFlag.getBooleanValue() == opinion.isLiked()) {
            return;
        }
        opinion.setLiked(likeHateFlag.getBooleanValue());
        userMovieRepository.save(opinion);
    }
}
