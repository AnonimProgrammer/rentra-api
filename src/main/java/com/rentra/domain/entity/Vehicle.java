package com.rentra.domain.entity;

import com.rentra.domain.enums.FuelType;
import com.rentra.domain.enums.TransmissionType;
import com.rentra.domain.enums.VehicleCategory;
import com.rentra.domain.enums.VehicleStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "vehicles")
public class Vehicle {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(columnDefinition = "BINARY(16)")
  private UUID id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private VehicleCategory category;

  private String brand;
  private String model;

  @Enumerated(EnumType.STRING)
  private TransmissionType transmission;

  @Enumerated(EnumType.STRING)
  private FuelType fuelType;

  private Integer seatCount;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private VehicleStatus status;

  @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<VehicleRate> rates = new ArrayList<>();

  @OneToMany(mappedBy = "vehicle")
  private List<Rent> rents = new ArrayList<>();

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;


  // @ManyToOne
  // @JoinColumn( name = "rental_service_id", nullable = false)
  // private RentalService rentalService;


  public Vehicle() {
  }

  public UUID getId() {
    return id;
  }

//  public RentalService getRentalService() {
//    return rentalService;
//  }
//
//  public void setRentalService(RentalService rentalService) {
//    this.rentalService = rentalService;
//  }

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

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public List<VehicleRate> getRates() {
    return rates;
  }

  public List<Rent> getRents() {
    return rents;
  }

}
