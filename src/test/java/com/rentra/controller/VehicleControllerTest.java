package com.rentra.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.rentra.domain.rent.RentStatus;
import com.rentra.domain.vehicle.FuelType;
import com.rentra.domain.vehicle.TransmissionType;
import com.rentra.domain.vehicle.VehicleCategory;
import com.rentra.domain.vehicle.VehicleStatus;
import com.rentra.dto.pagination.PageResponse;
import com.rentra.dto.pagination.PaginationMeta;
import com.rentra.dto.rent.RentResponse;
import com.rentra.dto.rental_agency.RentalAgencySummary;
import com.rentra.dto.vehicle.VehicleDetails;
import com.rentra.dto.vehicle.VehicleRateResponse;
import com.rentra.dto.vehicle.VehicleSearchRequest;
import com.rentra.dto.vehicle.VehicleSummary;
import com.rentra.exception.ResourceNotFoundException;
import com.rentra.service.rent.RentService;
import com.rentra.service.security.auth.AuthService;
import com.rentra.service.security.jwt.JwtAuthenticationFilter;
import com.rentra.service.vehicle.VehicleService;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VehicleController.class)
@AutoConfigureMockMvc(addFilters = false)
class VehicleControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VehicleService vehicleService;

    @MockitoBean
    private RentService rentService;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private AuthenticationEntryPoint authenticationEntryPoint;

    // =========================== createVehicle ===========================

    @Test
    @WithMockUser
    void createVehicle_shouldReturnCreatedVehicleDetails() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        UUID agencyId = UUID.randomUUID();

        VehicleDetails details = new VehicleDetails(
                vehicleId,
                new RentalAgencySummary(agencyId, "Rentra Baku", "Premium", null, null),
                VehicleCategory.SUV,
                "BMW",
                "X5",
                TransmissionType.AUTOMATIC,
                FuelType.PETROL,
                5,
                VehicleStatus.AVAILABLE,
                List.of());

        when(authService.getCurrentUserId()).thenReturn(userId);
        when(vehicleService.create(org.mockito.ArgumentMatchers.eq(userId), org.mockito.ArgumentMatchers.any()))
                .thenReturn(details);

        mockMvc.perform(post("/v1/vehicles")
                        .contentType("application/json")
                        .content(
                                """
                                {
                                  "rentalAgencyId": "%s",
                                  "category": "SUV",
                                  "brand": "BMW",
                                  "model": "X5",
                                  "transmission": "AUTOMATIC",
                                  "fuelType": "PETROL",
                                  "seatCount": 5,
                                  "rates": []
                                }
                                """
                                        .formatted(agencyId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(vehicleId.toString()))
                .andExpect(jsonPath("$.brand").value("BMW"));
    }

    @Test
    @WithMockUser
    void createVehicle_shouldReturnValidationError_whenRequiredFieldsMissing() throws Exception {
        mockMvc.perform(post("/v1/vehicles").contentType("application/json").content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.rentalAgencyId").value("Rental agency id is required"));
    }

    // =========================== getVehicleById ===========================

    @Test
    @WithMockUser
    void getVehicleById_shouldReturnVehicleDetails_whenFound() throws Exception {
        UUID vehicleId = UUID.randomUUID();
        VehicleDetails details = new VehicleDetails(
                vehicleId,
                null,
                VehicleCategory.SUV,
                "BMW",
                "X5",
                TransmissionType.AUTOMATIC,
                FuelType.PETROL,
                5,
                VehicleStatus.AVAILABLE,
                List.of(new VehicleRateResponse(
                        UUID.randomUUID(),
                        com.rentra.domain.vehicle.RateType.DAY,
                        java.math.BigDecimal.TEN,
                        com.rentra.domain.payment.Currency.AZN)));

        when(vehicleService.getDetails(vehicleId)).thenReturn(details);

        mockMvc.perform(get("/v1/vehicles/{id}", vehicleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(vehicleId.toString()))
                .andExpect(jsonPath("$.model").value("X5"));
    }

    @Test
    @WithMockUser
    void getVehicleById_shouldReturnNotFound_whenVehicleMissing() throws Exception {
        UUID vehicleId = UUID.randomUUID();
        when(vehicleService.getDetails(vehicleId)).thenThrow(new ResourceNotFoundException("Vehicle not found"));

        mockMvc.perform(get("/v1/vehicles/{id}", vehicleId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Vehicle not found"));
    }

    // =========================== searchVehicles ===========================

    @Test
    @WithMockUser
    void searchVehicles_shouldReturnPagedVehicleSummaries() throws Exception {
        UUID vehicleId = UUID.randomUUID();
        UUID agencyId = UUID.randomUUID();
        VehicleSummary summary = new VehicleSummary(
                vehicleId,
                agencyId,
                "BMW",
                "X5",
                VehicleCategory.SUV,
                TransmissionType.AUTOMATIC,
                FuelType.PETROL,
                5,
                VehicleStatus.AVAILABLE);
        PageResponse<VehicleSummary> page =
                new PageResponse<>(List.of(summary), new PaginationMeta(null, null, false, false, 10));

        when(vehicleService.search(org.mockito.ArgumentMatchers.any(VehicleSearchRequest.class)))
                .thenReturn(page);

        mockMvc.perform(get("/v1/vehicles/search").param("limit", "10").param("brand", "bmw"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(vehicleId.toString()))
                .andExpect(jsonPath("$.pagination.limit").value(10));
    }

    // =========================== getVehicleRentHistory ===========================

    @Test
    @WithMockUser
    void getVehicleRentHistory_shouldReturnValidationError_whenLimitInvalid() throws Exception {
        UUID vehicleId = UUID.randomUUID();

        mockMvc.perform(get("/v1/vehicles/{id}/rents/history", vehicleId).param("limit", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.limit").value("Limit must be at least 1"));
    }

    @Test
    @WithMockUser
    void getVehicleRentHistory_shouldReturnHistoryPage_whenRequestValid() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        UUID rentId = UUID.randomUUID();
        RentResponse rent = new RentResponse(
                rentId, UUID.randomUUID(), vehicleId, UUID.randomUUID(), null, null, RentStatus.ACTIVE, null, null);
        PageResponse<RentResponse> page =
                new PageResponse<>(List.of(rent), new PaginationMeta(null, null, false, false, 20));

        when(authService.getCurrentUserId()).thenReturn(userId);
        when(rentService.getRentHistoryByVehicleId(
                        org.mockito.ArgumentMatchers.eq(userId),
                        org.mockito.ArgumentMatchers.eq(vehicleId),
                        org.mockito.ArgumentMatchers.any()))
                .thenReturn(page);

        mockMvc.perform(get("/v1/vehicles/{id}/rents/history", vehicleId).param("limit", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(rentId.toString()))
                .andExpect(jsonPath("$.pagination.limit").value(20));
    }
}
