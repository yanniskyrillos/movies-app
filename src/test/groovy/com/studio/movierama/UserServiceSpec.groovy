package com.studio.movierama

import com.studio.movierama.converter.UserDtoToUserConverter
import com.studio.movierama.converter.UserToUserDtoConverter
import com.studio.movierama.domain.User
import com.studio.movierama.dto.UserDto
import com.studio.movierama.repository.UserRepository
import com.studio.movierama.service.UserService
import spock.lang.Specification

class UserServiceSpec extends Specification {

    UserRepository userRepository = Mock(UserRepository)
    UserToUserDtoConverter userToUserDtoConverter = Mock(UserToUserDtoConverter)
    UserDtoToUserConverter userDtoToUserConverter = Mock(UserDtoToUserConverter)
    UserService userService = new UserService(userRepository, userDtoToUserConverter, userToUserDtoConverter)

    def "save"() {
        given: "a request to save a user"
            UserDto userDto = new UserDto()
        when: "the method is called"
            userService.save(userDto)
        then: "the repository method is called once & conversionService twice"
            1 * userRepository.save(_)
            1 * userDtoToUserConverter.convert(_)
            1 * userToUserDtoConverter.convert(_)
    }

    def "find by username"() {
        when: "the method is called"
            userService.findByUsername("username")
        then: "repository and conversionService are each called once"
            1 * userRepository.findByUsername("username") >> Optional.of(new User())
            1 * userToUserDtoConverter.convert(_)
    }
}
