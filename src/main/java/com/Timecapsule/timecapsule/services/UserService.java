package com.Timecapsule.timecapsule.services;

import java.nio.CharBuffer;
import java.util.Optional;
import java.util.UUID;

import com.Timecapsule.timecapsule.dto.CredentialsDto;
import com.Timecapsule.timecapsule.exceptions.AppException;
import com.Timecapsule.timecapsule.dto.SignUpDto;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.Timecapsule.timecapsule.dto.UserDto;
import com.Timecapsule.timecapsule.models.User;
import com.Timecapsule.timecapsule.models.mappers.UserMapper;
import com.Timecapsule.timecapsule.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserService {

  private final UserRepository userRepository;

  private final PasswordEncoder passwordEncoder;

  private final UserMapper userMapper;

  public UserDto findByLogin(String email) {
    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
    return userMapper.toUserDto(user);
  }

  public UserDto login(CredentialsDto credentialsDto){
    User user = userRepository.findByEmail(credentialsDto.getEmail())
     .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));

    if (passwordEncoder.matches(CharBuffer.wrap(credentialsDto.getPassword()), user.getPassword())){
      return userMapper.toUserDto(user);
    }

    throw new AppException("Invalid credentials", HttpStatus.BAD_REQUEST);
  }

  public UserDto register(SignUpDto userDto) {
    Optional<User> optionalUser =  userRepository.findByEmail(userDto.getEmail());

    if(optionalUser.isPresent()){
      throw new AppException("User already exists", HttpStatus.BAD_REQUEST);
    }

    User user = userMapper.signUpToUser(userDto);

    user.setPassword(passwordEncoder.encode(CharBuffer.wrap(userDto.getPassword())));

    User savedUser =  userRepository.save(user);

    return userMapper.toUserDto(savedUser);
  }
}
