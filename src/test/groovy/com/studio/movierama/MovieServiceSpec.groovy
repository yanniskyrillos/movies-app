package com.studio.movierama

import com.studio.movierama.config.security.MovieRamaUserDetails
import com.studio.movierama.domain.Movie
import com.studio.movierama.domain.User
import com.studio.movierama.domain.Opinion
import com.studio.movierama.domain.OpinionId
import com.studio.movierama.dto.MovieDto
import com.studio.movierama.dto.MovieRatingRequestDto
import com.studio.movierama.enums.Rating
import com.studio.movierama.repository.MovieRepository
import com.studio.movierama.repository.UserMovieRepository
import com.studio.movierama.service.MovieService
import org.springframework.core.convert.ConversionService
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.context.SecurityContextImpl
import spock.lang.Specification

class MovieServiceSpec extends Specification {

    MovieRepository movieRepository = Mock(MovieRepository)
    UserMovieRepository userMovieRepository = Mock(UserMovieRepository)
    ConversionService conversionService = Mock(ConversionService)
    MovieService movieService = new MovieService(movieRepository, conversionService, userMovieRepository)

    def "save"() {
        given: "a request to save a movie"
            MovieDto movieDto = new MovieDto()
        when: "the method is called"
            movieService.save(movieDto)
        then: "the repository method is called once & conversionService twice"
            1 * conversionService.convert(_, Movie.class) >> new Movie()
            1 * movieRepository.save(_)
            1 * conversionService.convert(_, MovieDto.class)
    }

    def "retract rating"() {
        given: "a request to retract a movie rating"
            MovieRatingRequestDto movieRatingRequestDto = MovieRatingRequestDto.builder().retract(true).build()
        when: "the method is called"
            movieService.rate(movieRatingRequestDto)
        then: "delete method of userMovieRepository is called"
            1 * userMovieRepository.deleteById(_)
    }

    def "like a movie"() {
        given: "a request to like a movie"
            MovieRatingRequestDto movieRatingRequestDto = MovieRatingRequestDto
                    .builder()
                    .rating(Rating.LIKE)
                    .userId(1L)
                    .movieId(1L)
                    .build()
            OpinionId userMovieId = new OpinionId(1L, 1L)
            Opinion userMovie = Opinion
                    .builder()
                    .opinionId(userMovieId)
                    .likeHateFlag(Rating.LIKE.booleanValue)
                    .build()
        when: "the method is called"
            movieService.rate(movieRatingRequestDto)
        then: "an object with LIKE flag will be saved to the database"
            1 * movieRepository.findById(_) >> Optional.of(Movie.builder().userId(2L).id(1L).build())
            1 * userMovieRepository.findById(_) >> Optional.of(Opinion.builder().opinionId(userMovieId).build())
            1 * userMovieRepository.save(userMovie)
    }

    def "hate a movie"() {
        given: "a request to hate a movie"
            MovieRatingRequestDto movieRatingRequestDto = MovieRatingRequestDto
                    .builder()
                    .rating(Rating.DISLIKE)
                    .userId(1L)
                    .movieId(1L)
                    .build()
            OpinionId userMovieId = new OpinionId(1L, 1L)
            Opinion userMovie = Opinion
                    .builder()
                    .opinionId(userMovieId)
                    .likeHateFlag(Rating.DISLIKE.booleanValue)
                    .build()
        when: "the method is called"
            movieService.rate(movieRatingRequestDto)
        then: "an object with HATE flag will be saved to the database"
            1 * movieRepository.findById(_) >> Optional.of(Movie.builder().userId(2L).id(1L).build())
            1 * userMovieRepository.findById(_) >> Optional.of(Opinion.builder().opinionId(userMovieId).build())
            1 * userMovieRepository.save(userMovie)
    }

    def "find all movies with their ratings"() {
        given: "a request from a logged in user"
            Pageable pageable = Pageable.unpaged()
            User user = User.builder()
                            .id(1)
                            .username("user")
                            .password("pass")
                            .build()
            MovieRamaUserDetails movieRamaUserDetails = new MovieRamaUserDetails(user)
            Authentication authentication = new TestingAuthenticationToken(movieRamaUserDetails, "cred")
            SecurityContextImpl securityContext = new SecurityContextImpl(authentication)
            SecurityContextHolder.setContext(securityContext)
            Movie movie = Movie.builder()
                                .id(1)
                                .userId(2)
                                .build()
            OpinionId userMovieId = new OpinionId(2, 1)
            Opinion userMovie = new Opinion(userMovieId, "L")
            MovieDto movieDto = MovieDto.builder().userId(2).id(1).build()
        when: "the method is called"
            def response = movieService.findAll(pageable).content[0]
        then: "the response contains the expected rating"
            1 * movieRepository.findAll(pageable) >> new PageImpl<Movie>([movie])
            1 * conversionService.convert(_, MovieDto.class) >> movieDto
            1 * userMovieRepository.findAllById(_) >> List.of(userMovie)
            with(response) {
                likedByUser
                (!hatedByUser)
            }
    }
}
