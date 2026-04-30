package com.rentra.controller;

import java.time.OffsetDateTime;
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
import com.rentra.domain.reservation.ReservationStatus;
import com.rentra.dto.rent.RentResponse;
import com.rentra.dto.reservation.CreateReservationRequest;
import com.rentra.dto.reservation.ReservationResponse;
import com.rentra.exception.ConflictException;
import com.rentra.service.reservation.ReservationService;
import com.rentra.service.security.auth.AuthService;
import com.rentra.service.security.jwt.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReservationController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReservationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private AuthenticationEntryPoint authenticationEntryPoint;

    // =========================== reserveVehicle ===========================

    @Test
    @WithMockUser
    void reserveVehicle_shouldReturnCreatedReservation() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID reservationId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        UUID agencyId = UUID.randomUUID();

        CreateReservationRequest request = new CreateReservationRequest(vehicleId);
        ReservationResponse response = new ReservationResponse(
                reservationId,
                customerId,
                vehicleId,
                agencyId,
                ReservationStatus.RESERVED,
                OffsetDateTime.parse("2026-01-01T10:00:00Z"),
                null,
                null,
                OffsetDateTime.parse("2026-01-02T10:00:00Z"));

        when(authService.getCurrentUserId()).thenReturn(userId);
        when(reservationService.reserve(userId, request)).thenReturn(response);

        mockMvc.perform(post("/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {
                                  "vehicleId": "%s"
                                }
                                """
                                        .formatted(vehicleId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(reservationId.toString()))
                .andExpect(jsonPath("$.status").value("RESERVED"));
    }

    @Test
    @WithMockUser
    void reserveVehicle_shouldReturnValidationError_whenVehicleIdMissing() throws Exception {
        mockMvc.perform(post("/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.vehicleId").value("Vehicle id is required"));
    }

    // =========================== confirm ===========================

    @Test
    @WithMockUser
    void confirm_shouldReturnRentResponse_whenConfirmationSucceeds() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID reservationId = UUID.randomUUID();
        UUID rentId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        UUID agencyId = UUID.randomUUID();

        RentResponse response = new RentResponse(
                rentId,
                customerId,
                vehicleId,
                agencyId,
                null,
                null,
                RentStatus.ACTIVE,
                OffsetDateTime.parse("2026-01-01T11:00:00Z"),
                null);

        when(authService.getCurrentUserId()).thenReturn(userId);
        when(reservationService.confirm(userId, reservationId)).thenReturn(response);

        mockMvc.perform(post("/v1/reservations/{id}/confirm", reservationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(rentId.toString()))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @WithMockUser
    void confirm_shouldReturnConflictError_whenReservationCannotBeConfirmed() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID reservationId = UUID.randomUUID();

        when(authService.getCurrentUserId()).thenReturn(userId);
        when(reservationService.confirm(userId, reservationId))
                .thenThrow(new ConflictException("Reservation has expired"));

        mockMvc.perform(post("/v1/reservations/{id}/confirm", reservationId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value("Reservation has expired"));
    }
}
