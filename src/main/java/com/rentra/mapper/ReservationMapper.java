package com.rentra.mapper;

import com.rentra.domain.reservation.ReservationEntity;
import com.rentra.dto.reservation.ReservationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReservationMapper {
    @Mapping(source = "customer.id", target = "customerId")
    @Mapping(source = "vehicle.id", target = "vehicleId")
    @Mapping(source = "rentalAgency.id", target = "rentalAgencyId")
    ReservationResponse toResponse(ReservationEntity entity);
}
