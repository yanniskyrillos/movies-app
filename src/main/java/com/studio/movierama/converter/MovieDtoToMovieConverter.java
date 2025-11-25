package com.studio.movierama.converter;

import com.studio.movierama.domain.Movie;
import com.studio.movierama.dto.MovieDto;
import com.studio.movierama.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class MovieDtoToMovieConverter implements Converter<MovieDto, Movie> {

    @Autowired
    private UserService userService;

    @Override
    public Movie convert(MovieDto source) {
        Movie movie = Movie
                .builder()
                .title(source.getTitle())
                .description(source.getDescription())
                .submitter(userService.getReferenceById(source.getId()))
                .publicationDate(source.getPublicationDate())
                .likes(source.getLikes())
                .hates(source.getHates())
                .build();
        return movie;
    }
}
