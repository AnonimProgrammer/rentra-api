package com.rentra.mapper;

import java.util.ArrayList;
import java.util.List;

import com.rentra.domain.rental_service.RentalServiceEntity;
import com.rentra.dto.rental_service.RentalServiceRespond;

public class RentalServiceMapper {

    public static RentalServiceRespond toResponse(RentalServiceEntity rental) {
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

    public static List<RentalServiceRespond> toResponseList(List<RentalServiceEntity> rentals) {
        List<RentalServiceRespond> responds = new ArrayList<>();
        for (RentalServiceEntity rental : rentals) {
            responds.add(toResponse(rental));
        }
        return responds;
    }
}
