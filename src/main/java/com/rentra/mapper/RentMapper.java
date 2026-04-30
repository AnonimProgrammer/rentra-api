package com.rentra.mapper;

import com.rentra.domain.rent.RentEntity;
import com.rentra.dto.rent.RentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RentMapper {
    @Mapping(source = "customer.id", target = "customerId")
    @Mapping(source = "vehicle.id", target = "vehicleId")
    @Mapping(source = "rentalAgency.id", target = "rentalAgencyId")
    RentResponse toResponse(RentEntity entity);
}
