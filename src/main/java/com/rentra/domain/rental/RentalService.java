package com.rentra.domain.rental;

import com.rentra.domain.user.UserEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "rental_services")
public class RentalService {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  @ManyToOne
  @JoinColumn(name = "owner_user_id", referencedColumnName = "id", nullable = false)
  private UserEntity ownerUser;

  @NotBlank
  @Size(max = 150, min = 2, message = "Rental Service name must be between 2 and 100 characters")
  private String name;

  @Size(max = 500, min = 2, message = "Description must be between 2 and 500 characters")
  private String description;

  @Column(precision = 10, scale = 7, nullable = false, name = "location_lat")
  private BigDecimal locationLat;

  @Column(precision = 10, scale = 7, nullable = false, name = "location_lng")
  private BigDecimal locationLng;

  @NotNull
  @Enumerated(EnumType.STRING)
  private RentalServiceStatus status = RentalServiceStatus.ACTIVE;

  @NotNull
  @CreationTimestamp
  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @NotNull
  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  public RentalService() {}

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public UserEntity getOwnerUser() {
    return ownerUser;
  }

  public void setOwnerUser(UserEntity ownerUser) {
    this.ownerUser = ownerUser;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public RentalServiceStatus getStatus() {
    return status;
  }

  public void setStatus(RentalServiceStatus status) {
    this.status = status;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  public BigDecimal getLocationLat() {
    return locationLat;
  }

  public void setLocationLat(BigDecimal locationLat) {
    this.locationLat = locationLat;
  }

  public BigDecimal getLocationLng() {
    return locationLng;
  }

  public void setLocationLng(BigDecimal locationLng) {
    this.locationLng = locationLng;
  }
}
