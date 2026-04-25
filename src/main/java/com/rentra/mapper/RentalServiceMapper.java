package com.rentra.mapper;

import java.util.ArrayList;
import java.util.List;

import com.rentra.domain.rental_service.RentalService;
import com.rentra.dto.rental_service.RentalServiceRespond;

public class RentalServiceMapper {

    public static RentalServiceRespond toResponse(RentalService rental) {
        RentalServiceRespond respond = new RentalServiceRespond();
        respond.setId(rental.getId());
        respond.setName(rental.getName());
        respond.setDescription(rental.getDescription());
        respond.setLocationLat(rental.getLocationLat());
        respond.setLocationLng(rental.getLocationLng());
        respond.setOwner(rental.getOwnerUser().getFirstName() + " "
                + rental.getOwnerUser().getLastName());
        return respond;
    }

    public static List<RentalServiceRespond> toResponseList(List<RentalService> rentals) {
        List<RentalServiceRespond> responds = new ArrayList<>();
        for (RentalService rental : rentals) {
            responds.add(toResponse(rental));
        }
        return responds;
    }
}
