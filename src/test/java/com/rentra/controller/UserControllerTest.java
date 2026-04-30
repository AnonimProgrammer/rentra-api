package com.rentra.controller;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.rentra.domain.auth.RoleName;
import com.rentra.domain.user.UserStatus;
import com.rentra.dto.user.MeResponse;
import com.rentra.dto.user.UserResponse;
import com.rentra.exception.ResourceNotFoundException;
import com.rentra.service.security.auth.AuthService;
import com.rentra.service.security.jwt.JwtAuthenticationFilter;
import com.rentra.service.user.UserService;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private AuthenticationEntryPoint authenticationEntryPoint;

    // =========================== me ===========================

    @Test
    @WithMockUser
    void me_shouldReturnCurrentUserProfile() throws Exception {
        UUID currentUserId = UUID.randomUUID();
        MeResponse response = new MeResponse(
                currentUserId,
                "Omar",
                "Ismayilov",
                "omar.ismayilov@example.com",
                "+994 99 999 99 99",
                LocalDate.of(1995, 5, 15),
                UserStatus.ACTIVE,
                OffsetDateTime.parse("2026-01-01T10:00:00Z"));

        when(authService.getCurrentUserId()).thenReturn(currentUserId);
        when(userService.getMeById(currentUserId)).thenReturn(response);

        mockMvc.perform(get("/v1/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(currentUserId.toString()))
                .andExpect(jsonPath("$.firstName").value("Omar"))
                .andExpect(jsonPath("$.lastName").value("Ismayilov"))
                .andExpect(jsonPath("$.phoneNumber").value("+994 99 999 99 99"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @WithMockUser
    void me_shouldReturnNotFound_whenUserDoesNotExist() throws Exception {
        UUID currentUserId = UUID.randomUUID();
        when(authService.getCurrentUserId()).thenReturn(currentUserId);
        when(userService.getMeById(currentUserId)).thenThrow(new ResourceNotFoundException("User not found"));

        mockMvc.perform(get("/v1/users/me"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    // =========================== getById ===========================

    @Test
    @WithMockUser(roles = "ADMIN")
    void getById_shouldReturnUser_whenRequesterIsAdmin() throws Exception {
        UUID userId = UUID.randomUUID();
        UserResponse response = new UserResponse(
                userId,
                "Omar",
                "Ismayilov",
                "omar.ismayilov@example.com",
                "+994 99 999 99 99",
                LocalDate.of(1995, 5, 15),
                UserStatus.ACTIVE,
                OffsetDateTime.parse("2026-01-01T10:00:00Z"),
                OffsetDateTime.parse("2026-01-02T10:00:00Z"),
                List.of(RoleName.ADMIN));

        when(userService.getById(userId)).thenReturn(response);

        mockMvc.perform(get("/v1/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.email").value("omar.ismayilov@example.com"))
                .andExpect(jsonPath("$.roles[0]").value("ADMIN"));
    }
}
