package com.rentra.mapper;

import java.util.ArrayList;
import java.util.List;

import com.rentra.domain.rental_agency.RentalAgencyEntity;
import com.rentra.dto.rental_agency.RentalAgencyResponse;

public class RentalAgencyMapper {

    public static RentalAgencyResponse toResponse(RentalAgencyEntity rentalAgency) {
        RentalAgencyResponse response = new RentalAgencyResponse();
        response.setId(rentalAgency.getId());
        response.setName(rentalAgency.getName());
        response.setDescription(rentalAgency.getDescription());
        response.setLocationLat(rentalAgency.getLocationLat());
        response.setLocationLng(rentalAgency.getLocationLng());
        response.setOwner(rentalAgency.getOwnerUser().getFirstName() + " "
                + rentalAgency.getOwnerUser().getLastName());
        return response;
    }

    public static List<RentalAgencyResponse> toResponseList(List<RentalAgencyEntity> rentalAgencies) {
        List<RentalAgencyResponse> responses = new ArrayList<>();
        for (RentalAgencyEntity rentalAgency : rentalAgencies) {
            responses.add(toResponse(rentalAgency));
        }
        return responses;
    }
}
