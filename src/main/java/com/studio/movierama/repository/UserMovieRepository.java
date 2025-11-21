package com.studio.movierama.repository;

import com.studio.movierama.domain.Rating;
import com.studio.movierama.domain.RatingId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserMovieRepository extends JpaRepository<Rating, RatingId> {
}
