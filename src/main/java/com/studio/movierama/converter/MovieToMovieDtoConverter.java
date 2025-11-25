package com.studio.movierama.converter;

import com.studio.movierama.domain.Movie;
import com.studio.movierama.dto.MovieDto;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class MovieToMovieDtoConverter implements Converter<Movie, MovieDto> {

    @Override
    public MovieDto convert(Movie source) {
        MovieDto movieDto = MovieDto
                .builder()
                .id(source.getId())
                .title(source.getTitle())
                .description(source.getDescription())
                .publicationDate(source.getPublicationDate())
                .userId(source.getSubmitter().getId())
                .likes(source.getLikes())
                .dislikes(source.getDislikes())
                .build();
        return movieDto;
    }
}
