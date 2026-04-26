package com.rentra.service.user;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.rentra.domain.user.UserEntity;
import com.rentra.dto.user.MeResponse;
import com.rentra.dto.user.UserResponse;
import com.rentra.exception.ResourceNotFoundException;
import com.rentra.mapper.UserMapper;
import com.rentra.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserEntity findOrThrow(UUID id) {
        return userRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for id: " + id));
    }

    public MeResponse getMeById(UUID id) {
        UserEntity user = findOrThrow(id);
        return userMapper.toMeResponse(user);
    }

    public UserResponse getById(UUID id) {
        UserEntity user = findOrThrow(id);
        return userMapper.toUserResponse(user);
    }
}
