package com.rentra.mapper;

import com.rentra.domain.rental_agency.RentalAgencyEntity;
import com.rentra.domain.vehicle.VehicleEntity;
import com.rentra.domain.vehicle.VehicleRateEntity;
import com.rentra.dto.vehicle.CreateVehicleRequest;
import com.rentra.dto.vehicle.VehicleDetails;
import com.rentra.dto.vehicle.VehicleRateResponse;
import com.rentra.dto.vehicle.VehicleSummary;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = RentalAgencyMapper.class)
public interface VehicleMapper {
    @Mapping(target = "rentalAgency", source = "entity")
    @Mapping(target = "status", constant = "AVAILABLE")
    VehicleEntity toEntity(CreateVehicleRequest request, RentalAgencyEntity entity);

    VehicleSummary toSummary(VehicleEntity entity);

    VehicleDetails toDetails(VehicleEntity entity);

    default VehicleRateResponse toRateResponse(VehicleRateEntity entity) {
        return new VehicleRateResponse(entity.getId(), entity.getType(), entity.getPrice(), entity.getCurrency());
    }
}
