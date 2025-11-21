package com.studio.movierama.dto;

import com.studio.movierama.enums.Rating;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MovieRatingRequestDto {

    private Long userId;
    private Long movieId;
    private Rating rating;
    private boolean retract;
}
