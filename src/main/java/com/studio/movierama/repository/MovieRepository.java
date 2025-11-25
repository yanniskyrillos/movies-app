package com.studio.movierama.repository;

import com.studio.movierama.domain.Movie;
import com.studio.movierama.dto.MovieDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    @Query("""
           select new com.studio.movierama.dto.MovieDto(
             m.id,
             m.title,
             m.description,
             u.id,
             m.publicationDate,
             m.likes,
             m.dislikes,
             u.username,
             false,
             false
           )
           from Movie m
           join m.submitter u
            """)
    Page<MovieDto> findAllMovieDetails(Pageable pageable);
}
