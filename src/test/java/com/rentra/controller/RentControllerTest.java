package com.rentra.controller;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.rentra.domain.rent.RentStatus;
import com.rentra.dto.pagination.PageResponse;
import com.rentra.dto.pagination.PaginationMeta;
import com.rentra.dto.rent.RentResponse;
import com.rentra.exception.ResourceNotFoundException;
import com.rentra.service.rent.RentService;
import com.rentra.service.security.auth.AuthService;
import com.rentra.service.security.jwt.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RentController.class)
@AutoConfigureMockMvc(addFilters = false)
class RentControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RentService rentService;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private AuthenticationEntryPoint authenticationEntryPoint;

    // =========================== complete ===========================

    @Test
    @WithMockUser
    void complete_shouldReturnCompletedRent() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID rentId = UUID.randomUUID();
        RentResponse response = new RentResponse(
                rentId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                null,
                RentStatus.COMPLETED,
                OffsetDateTime.parse("2026-01-01T10:00:00Z"),
                OffsetDateTime.parse("2026-01-01T12:00:00Z"));

        when(authService.getCurrentUserId()).thenReturn(userId);
        when(rentService.complete(rentId, userId)).thenReturn(response);

        mockMvc.perform(post("/v1/rents/{id}/complete", rentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(rentId.toString()))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    // =========================== rate ===========================

    @Test
    @WithMockUser
    void rate_shouldReturnUpdatedRent_whenRequestValid() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID rentId = UUID.randomUUID();
        RentResponse response = new RentResponse(
                rentId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                5,
                RentStatus.COMPLETED,
                OffsetDateTime.parse("2026-01-01T10:00:00Z"),
                OffsetDateTime.parse("2026-01-01T12:00:00Z"));

        when(authService.getCurrentUserId()).thenReturn(userId);
        when(rentService.rate(rentId, userId, 5)).thenReturn(response);

        mockMvc.perform(
                        post("/v1/rents/{id}/rate", rentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {
                                  "rating": 5
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(rentId.toString()))
                .andExpect(jsonPath("$.rating").value(5));
    }

    @Test
    @WithMockUser
    void rate_shouldReturnValidationError_whenRatingMissing() throws Exception {
        UUID rentId = UUID.randomUUID();

        mockMvc.perform(post("/v1/rents/{id}/rate", rentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.rating").value("Rating is required"));
    }

    // =========================== getMyRents ===========================

    @Test
    @WithMockUser
    void getMyRents_shouldReturnPaginatedHistory() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID rentId = UUID.randomUUID();
        UUID cursor = UUID.randomUUID();
        RentResponse rent = new RentResponse(
                rentId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                null,
                RentStatus.ACTIVE,
                OffsetDateTime.parse("2026-01-01T10:00:00Z"),
                null);
        PageResponse<RentResponse> page =
                new PageResponse<>(List.of(rent), new PaginationMeta(null, cursor.toString(), false, true, 10));

        when(authService.getCurrentUserId()).thenReturn(userId);
        when(rentService.getMyRents(userId, cursor, 10, RentStatus.ACTIVE)).thenReturn(page);

        mockMvc.perform(get("/v1/rents/me/history")
                        .param("cursor", cursor.toString())
                        .param("limit", "10")
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(rentId.toString()))
                .andExpect(jsonPath("$.pagination.limit").value(10))
                .andExpect(jsonPath("$.pagination.hasPrevious").value(true));
    }

    // =========================== getMyActiveRent ===========================

    @Test
    @WithMockUser
    void getMyActiveRent_shouldReturnError_whenNoActiveRentExists() throws Exception {
        UUID userId = UUID.randomUUID();
        when(authService.getCurrentUserId()).thenReturn(userId);
        when(rentService.getMyActive(userId))
                .thenThrow(new ResourceNotFoundException("User does not have an active rent"));

        mockMvc.perform(get("/v1/rents/me/active"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("User does not have an active rent"));
    }
}
