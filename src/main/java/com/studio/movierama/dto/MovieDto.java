package com.studio.movierama.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MovieDto {

    private Long id;
    private String title;
    private String description;
    private Long userId;
    private Instant publicationDate;
    private Integer likes;
    private Integer dislikes;
    private String username;
    private boolean likedByUser;
    private boolean dislikedByUser;
}
