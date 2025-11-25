package com.studio.movierama.service;

import com.studio.movierama.domain.User;
import com.studio.movierama.dto.UserDto;
import com.studio.movierama.exception.MovieRamaException;
import com.studio.movierama.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class UserService {

    private UserRepository userRepository;
    private ConversionService conversionService;

    @Autowired
    public UserService(UserRepository userRepository, ConversionService conversionService) {
        this.userRepository = userRepository;
        this.conversionService = conversionService;
    }

    public UserDto save(UserDto userDto) {
        log.info("Saving user");
        User user = conversionService.convert(userDto, User.class);
        try {
            userRepository.save(user);
        } catch (Exception unique) {
            log.error(unique.getMessage());
            throw new MovieRamaException("User already exists");
        }
        userDto = conversionService.convert(user, UserDto.class);
        return userDto;
    }

    public UserDto findByUsername(String username) {
        log.info("find user by username");
        Optional<User> user = userRepository.findByUsername(username);
        UserDto userDto = user
                .map(user1 -> conversionService.convert(user1, UserDto.class))
                .orElse(null);
        return userDto;
    }

    public User getReferenceById(Long id) {
        User user = userRepository.getReferenceById(id);
        return user;
    }
}
