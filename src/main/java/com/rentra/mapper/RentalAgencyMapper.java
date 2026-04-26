package com.rentra.mapper;

import com.rentra.domain.rental_agency.RentalAgencyEntity;
import com.rentra.dto.rental_agency.RentalAgencyResponse;
import com.rentra.dto.rental_agency.RentalAgencySummary;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface RentalAgencyMapper {
    @Mapping(source = "ownerUser", target = "owner")
    RentalAgencyResponse toResponse(RentalAgencyEntity entity);

    RentalAgencySummary toSummary(RentalAgencyEntity entity);
}
