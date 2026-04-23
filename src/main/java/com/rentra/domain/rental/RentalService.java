package com.rentra.domain.rental;

import com.github.f4b6a3.ulid.UlidCreator;
import com.rentra.domain.user.UserEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "rental_services")
public class RentalService {
  @Id
  @JdbcTypeCode(SqlTypes.BINARY)
  @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
  private UUID id = UlidCreator.getUlid().toUuid();

  @ManyToOne(optional = false)
  @JoinColumn(name = "owner_user_id", nullable = false)
  private UserEntity ownerUser;

  @Column(name = "name", nullable = false, columnDefinition = "TEXT")
  private String name;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Column(precision = 10, scale = 7, name = "location_lat", columnDefinition = "DECIMAL(10,7)")
  private BigDecimal locationLat;

  @Column(precision = 10, scale = 7, name = "location_lng", columnDefinition = "DECIMAL(10,7)")
  private BigDecimal locationLng;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, columnDefinition = "TEXT")
  private RentalServiceStatus status = RentalServiceStatus.ACTIVE;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private OffsetDateTime updatedAt;

  public RentalService() {}

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

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public OffsetDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(OffsetDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  public RentalServiceStatus getStatus() {
    return status;
  }

  public void setStatus(RentalServiceStatus status) {
    this.status = status;
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

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }
}
