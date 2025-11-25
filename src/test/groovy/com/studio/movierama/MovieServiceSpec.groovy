package com.studio.movierama

import com.studio.movierama.config.security.MovieRamaUserDetails
import com.studio.movierama.domain.Movie
import com.studio.movierama.domain.User
import com.studio.movierama.domain.RatingId
import com.studio.movierama.dto.MovieDto
import com.studio.movierama.dto.MovieRatingRequestDto
import com.studio.movierama.enums.Rating
import com.studio.movierama.repository.MovieRepository
import com.studio.movierama.repository.RatingRepository
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
    RatingRepository ratingRepository = Mock(RatingRepository)
    ConversionService conversionService = Mock(ConversionService)
    MovieService movieService = new MovieService(movieRepository, conversionService, ratingRepository)

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
        then: "delete method of ratingRepository is called"
            1 * ratingRepository.deleteById(_)
    }

    def "like a movie"() {
        given: "a request to like a movie"
            MovieRatingRequestDto movieRatingRequestDto = MovieRatingRequestDto
                    .builder()
                    .rating(Rating.LIKE)
                    .userId(1L)
                    .movieId(1L)
                    .build()
            RatingId ratingId = new RatingId(1L, 1L)
            com.studio.movierama.domain.Rating rating = com.studio.movierama.domain.Rating
                    .builder()
                    .ratingId(ratingId)
                    .liked(Rating.LIKE.booleanValue)
                    .build()
        when: "the method is called"
            movieService.rate(movieRatingRequestDto)
        then: "an object with LIKE flag will be saved to the database"
            1 * movieRepository.findById(_) >> Optional.of(Movie.builder().userId(2L).id(1L).build())
            1 * ratingRepository.findById(_) >> Optional.of(com.studio.movierama.domain.Rating.builder().ratingId(ratingId).build())
            1 * ratingRepository.save(rating)
    }

    def "hate a movie"() {
        given: "a request to hate a movie"
            MovieRatingRequestDto movieRatingRequestDto = MovieRatingRequestDto
                    .builder()
                    .rating(Rating.DISLIKE)
                    .userId(1L)
                    .movieId(1L)
                    .build()
            RatingId ratingId = new RatingId(1L, 1L)
            com.studio.movierama.domain.Rating rating = com.studio.movierama.domain.Rating
                    .builder()
                    .ratingId(ratingId)
                    .liked(Rating.DISLIKE.booleanValue)
                    .build()
        when: "the method is called"
            movieService.rate(movieRatingRequestDto)
        then: "an object with HATE flag will be saved to the database"
            1 * movieRepository.findById(_) >> Optional.of(Movie.builder().userId(2L).id(1L).build())
            1 * ratingRepository.findById(_) >> Optional.of(com.studio.movierama.domain.Rating.builder().ratingId(ratingId).build())
            1 * ratingRepository.save(rating)
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
                                .submitter(User.builder().id(2).build())
                                .build()
            RatingId ratingId = new RatingId(2, 1)
            com.studio.movierama.domain.Rating rating = com.studio.movierama.domain.Rating.builder().ratingId(ratingId).liked(true)
            MovieDto movieDto = MovieDto.builder().userId(2).id(1).build()
        when: "the method is called"
            def response = movieService.findAll(pageable).content[0]
        then: "the response contains the expected rating"
            1 * movieRepository.findAll(pageable) >> new PageImpl<Movie>([movie])
            1 * conversionService.convert(_, MovieDto.class) >> movieDto
            1 * ratingRepository.findAllById(_) >> List.of(rating)
            with(response) {
                likedByUser
                (!hatedByUser)
            }
    }
}
