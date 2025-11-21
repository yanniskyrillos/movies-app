package com.studio.movierama.repository;

import com.studio.movierama.domain.Opinion;
import com.studio.movierama.domain.OpinionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserMovieRepository extends JpaRepository<Opinion, OpinionId> {
}
