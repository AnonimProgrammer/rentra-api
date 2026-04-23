package com.rentra.domain.entity;

import com.rentra.domain.enums.RentStatus;
import com.rentra.domain.user.UserEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "rents")
public class Rent {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(columnDefinition = "BINARY(16)")
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "customer_id", nullable = false)
  private UserEntity customer;

  @ManyToOne
  @JoinColumn(name = "vehicle_id", nullable = false)
  private Vehicle vehicle;

  private BigDecimal totalAmount;

  private Integer rating;

  @Enumerated(EnumType.STRING)
  private RentStatus status;

  @Column(name = "starts_at", nullable = false)
  private LocalDateTime startsAt;

  @Column(name = "completed_at")
  private LocalDateTime completedAt;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  public Rent() {
  }

  public UUID getId() {
    return id;
  }

  public UserEntity getCustomer() {
    return customer;
  }

  public void setCustomer(UserEntity customer) {
    this.customer = customer;
  }

  public Vehicle getVehicle() {
    return vehicle;
  }

  public void setVehicle(Vehicle vehicle) {
    this.vehicle = vehicle;
  }

  public BigDecimal getTotalAmount() {
    return totalAmount;
  }

  public void setTotalAmount(BigDecimal totalAmount) {
    this.totalAmount = totalAmount;
  }

  public Integer getRating() {
    return rating;
  }

  public void setRating(Integer rating) {
    this.rating = rating;
  }

  public RentStatus getStatus() {
    return status;
  }

  public void setStatus(RentStatus status) {
    this.status = status;
  }

  public LocalDateTime getStartsAt() {
    return startsAt;
  }

  public void setStartsAt(LocalDateTime startsAt) {
    this.startsAt = startsAt;
  }

  public LocalDateTime getCompletedAt() {
    return completedAt;
  }

  public void setCompletedAt(LocalDateTime completedAt) {
    this.completedAt = completedAt;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

}
