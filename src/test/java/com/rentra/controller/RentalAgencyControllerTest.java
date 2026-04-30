package com.rentra.controller;

import java.math.BigDecimal;
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

import com.rentra.domain.rental_agency.AgencyRole;
import com.rentra.domain.user.UserStatus;
import com.rentra.dto.rental_agency.CreateRentalAgencyRequest;
import com.rentra.dto.rental_agency.MyAgencyMembershipResponse;
import com.rentra.dto.rental_agency.RentalAgencyResponse;
import com.rentra.dto.rental_agency.RequestJoinResponse;
import com.rentra.service.rental_agency.AgencyMembershipService;
import com.rentra.service.rental_agency.RentalAgencyService;
import com.rentra.service.reservation.ReservationService;
import com.rentra.service.security.auth.AgencyAuthService;
import com.rentra.service.security.auth.AuthService;
import com.rentra.service.security.jwt.JwtAuthenticationFilter;
import com.rentra.service.vehicle.VehicleService;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RentalAgencyController.class)
@AutoConfigureMockMvc(addFilters = false)
class RentalAgencyControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RentalAgencyService rentalAgencyService;

    @MockitoBean
    private AgencyMembershipService agencyMembershipService;

    @MockitoBean
    private AgencyAuthService agencyAuthService;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private VehicleService vehicleService;

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private AuthenticationEntryPoint authenticationEntryPoint;

    // =========================== create ===========================

    @Test
    @WithMockUser
    void create_shouldReturnCreatedAgency() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID agencyId = UUID.randomUUID();
        CreateRentalAgencyRequest request = new CreateRentalAgencyRequest(
                "Rentra Baku", "Premium rentals", new BigDecimal("40.4092640"), new BigDecimal("49.8670920"));
        RentalAgencyResponse response = new RentalAgencyResponse(
                agencyId,
                "Rentra Baku",
                "Premium rentals",
                null,
                new BigDecimal("40.4092640"),
                new BigDecimal("49.8670920"));

        when(authService.getCurrentUserId()).thenReturn(userId);
        when(rentalAgencyService.create(request, userId)).thenReturn(response);

        String requestBody =
                """
                {
                  "name": "Rentra Baku",
                  "description": "Premium rentals",
                  "locationLat": 40.4092640,
                  "locationLng": 49.8670920
                }
                """;

        mockMvc.perform(post("/v1/agencies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(agencyId.toString()))
                .andExpect(jsonPath("$.name").value("Rentra Baku"));
    }

    // =========================== getMyMemberships ===========================

    @Test
    @WithMockUser
    void getMyMemberships_shouldReturnMembershipList() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID agencyId = UUID.randomUUID();
        MyAgencyMembershipResponse membership = new MyAgencyMembershipResponse(
                agencyId,
                "Rentra Baku",
                AgencyRole.MANAGER,
                UserStatus.ACTIVE,
                OffsetDateTime.parse("2026-01-01T10:00:00Z"));

        when(authService.getCurrentUserId()).thenReturn(userId);
        when(agencyMembershipService.getMyMemberships(userId)).thenReturn(List.of(membership));

        mockMvc.perform(get("/v1/agencies/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].agencyId").value(agencyId.toString()))
                .andExpect(jsonPath("$[0].agencyName").value("Rentra Baku"))
                .andExpect(jsonPath("$[0].role").value("MANAGER"));
    }

    // =========================== requestJoin ===========================

    @Test
    @WithMockUser
    void requestJoin_shouldReturnCreatedJoinRequest() throws Exception {
        UUID agencyId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        RequestJoinResponse response = new RequestJoinResponse(userId, agencyId, UserStatus.PENDING);

        when(authService.getCurrentUserId()).thenReturn(userId);
        when(agencyAuthService.requestAuthorization(agencyId, userId)).thenReturn(response);

        mockMvc.perform(post("/v1/agencies/{id}/join-requests", agencyId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.requestedUserId").value(userId.toString()))
                .andExpect(jsonPath("$.agencyId").value(agencyId.toString()))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    // =========================== updateMembership ===========================

    @Test
    @WithMockUser
    void updateMembership_shouldReturnValidationError_whenUserIdMissing() throws Exception {
        UUID agencyId = UUID.randomUUID();
        String invalidBody =
                """
                {
                  "status": "ACTIVE",
                  "role": "MANAGER"
                }
                """;

        mockMvc.perform(patch("/v1/agencies/{id}/memberships", agencyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.userId").value("User id is required"));
    }
}
