package com.rentra.domain.vehicle;

import com.github.f4b6a3.ulid.UlidCreator;
import com.rentra.domain.rental_service.RentalService;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "vehicles")
public class Vehicle {
  @Id
  @JdbcTypeCode(SqlTypes.BINARY)
  @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
  private UUID id = UlidCreator.getUlid().toUuid();

  @ManyToOne(optional = false)
  @JoinColumn(name = "rental_service_id", nullable = false)
  private RentalService rentalService;

  @Enumerated(EnumType.STRING)
  @Column(name = "category", nullable = false, columnDefinition = "TEXT")
  private VehicleCategory category;

  @Column(name = "brand", nullable = false, columnDefinition = "TEXT")
  private String brand;

  @Column(name = "model", nullable = false, columnDefinition = "TEXT")
  private String model;

  @Enumerated(EnumType.STRING)
  @Column(name = "transmission", nullable = false, columnDefinition = "TEXT")
  private TransmissionType transmission;

  @Enumerated(EnumType.STRING)
  @Column(name = "fuel_type", nullable = false, columnDefinition = "TEXT")
  private FuelType fuelType;

  @Column(name = "seat_count", nullable = false)
  private Integer seatCount;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, columnDefinition = "TEXT")
  private VehicleStatus status;

  @OneToMany(mappedBy = "vehicle")
  private List<VehicleRate> rates = new ArrayList<>();

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  public Vehicle() {}

  public UUID getId() {
    return id;
  }

  public RentalService getRentalService() {
    return rentalService;
  }

  public void setRentalService(RentalService rentalService) {
    this.rentalService = rentalService;
  }

  public VehicleCategory getCategory() {
    return category;
  }

  public void setCategory(VehicleCategory category) {
    this.category = category;
  }

  public String getBrand() {
    return brand;
  }

  public void setBrand(String brand) {
    this.brand = brand;
  }

  public String getModel() {
    return model;
  }

  public void setModel(String model) {
    this.model = model;
  }

  public TransmissionType getTransmission() {
    return transmission;
  }

  public void setTransmission(TransmissionType transmission) {
    this.transmission = transmission;
  }

  public FuelType getFuelType() {
    return fuelType;
  }

  public void setFuelType(FuelType fuelType) {
    this.fuelType = fuelType;
  }

  public Integer getSeatCount() {
    return seatCount;
  }

  public void setSeatCount(Integer seatCount) {
    this.seatCount = seatCount;
  }

  public VehicleStatus getStatus() {
    return status;
  }

  public void setStatus(VehicleStatus status) {
    this.status = status;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public OffsetDateTime getUpdatedAt() {
    return updatedAt;
  }

  public List<VehicleRate> getRates() {
    return rates;
  }
}
