package com.rentra.mapper;

import com.rentra.domain.rental_agency.RentalAgencyEntity;
import com.rentra.domain.vehicle.VehicleEntity;
import com.rentra.domain.vehicle.VehicleRateEntity;
import com.rentra.dto.vehicle.*;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = RentalAgencyMapper.class)
public interface VehicleMapper {
    @Mapping(target = "rentalAgency", source = "entity")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "rates", source = "request.rates")
    VehicleEntity toEntity(CreateVehicleRequest request, RentalAgencyEntity entity);

    @Mapping(target = "type", source = "rateType")
    @Mapping(target = "vehicle", ignore = true)
    VehicleRateEntity toRateEntity(CreateVehicleRateRequest request);

    @AfterMapping
    default void linkRates(@MappingTarget VehicleEntity vehicle) {
        if (vehicle.getRates() == null) {
            return;
        }
        vehicle.getRates().forEach(rate -> rate.setVehicle(vehicle));
    }

    VehicleSummary toSummary(VehicleEntity entity);

    VehicleDetails toDetails(VehicleEntity entity);

    default VehicleRateResponse toRateResponse(VehicleRateEntity entity) {
        return new VehicleRateResponse(entity.getId(), entity.getType(), entity.getPrice(), entity.getCurrency());
    }
}
