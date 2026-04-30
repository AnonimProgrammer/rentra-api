package com.rentra.service.user;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.rentra.domain.user.UserEntity;
import com.rentra.domain.user.UserStatus;
import com.rentra.dto.user.MeResponse;
import com.rentra.dto.user.UserResponse;
import com.rentra.exception.ResourceNotFoundException;
import com.rentra.mapper.UserMapper;
import com.rentra.repository.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    // =========================== findOrThrow ===========================

    @Test
    void findOrThrow_shouldReturnUser_whenUserExists() {
        UUID userId = UUID.randomUUID();
        UserEntity user = createUser(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserEntity result = userService.findOrThrow(userId);

        assertSame(user, result);
        verify(userRepository).findById(userId);
    }

    @Test
    void findOrThrow_shouldThrowResourceNotFoundException_whenUserDoesNotExist() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(ResourceNotFoundException.class, () -> userService.findOrThrow(userId));

        assertEquals("User not found for id: " + userId, exception.getMessage());
        verify(userRepository).findById(userId);
    }

    // =========================== getMeById ===========================

    @Test
    void getMeById_shouldMapFoundUserToMeResponse() {
        UUID userId = UUID.randomUUID();
        UserEntity user = createUser(userId);
        MeResponse expectedResponse = new MeResponse(
                userId,
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getBirthDate(),
                user.getStatus(),
                user.getCreatedAt());

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toMeResponse(user)).thenReturn(expectedResponse);

        MeResponse result = userService.getMeById(userId);

        assertEquals(expectedResponse, result);
        verify(userRepository).findById(userId);
        verify(userMapper).toMeResponse(user);
    }

    // =========================== getById ===========================

    @Test
    void getById_shouldMapFoundUserToUserResponse() {
        UUID userId = UUID.randomUUID();
        UserEntity user = createUser(userId);
        UserResponse expectedResponse = new UserResponse(
                userId,
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getBirthDate(),
                user.getStatus(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                List.of());

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toUserResponse(user)).thenReturn(expectedResponse);

        UserResponse result = userService.getById(userId);

        assertEquals(expectedResponse, result);
        verify(userRepository).findById(userId);
        verify(userMapper).toUserResponse(user);
    }

    private UserEntity createUser(UUID id) {
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setFirstName("Omar");
        user.setLastName("Ismayilov");
        user.setEmail("omar.ismayilov@example.com");
        user.setPhoneNumber("+994 99 999 99 99");
        user.setBirthDate(LocalDate.of(1995, 5, 15));
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(OffsetDateTime.parse("2026-01-01T10:00:00Z"));
        user.setUpdatedAt(OffsetDateTime.parse("2026-01-02T10:00:00Z"));
        return user;
    }
}
