package com.rentra.dto.rent;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record RateRentRequest(
        @NotNull(message = "Rating is required")
                @Min(value = 0, message = "Rating can not be less than 0")
                @Max(value = 5, message = "Rating can not be more than 5")
                Integer rating) {}
