package com.rentra.domain.vehicle;

import com.github.f4b6a3.ulid.UlidCreator;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "vehicle_rates")
public class VehicleRate {

  @Id
  @JdbcTypeCode(SqlTypes.BINARY)
  @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
  private UUID id = UlidCreator.getUlid().toUuid();

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false, columnDefinition = "TEXT")
  private RateType type;

  @Column(
      name = "price",
      nullable = false,
      precision = 10,
      scale = 2,
      columnDefinition = "DECIMAL(10,2)")
  private BigDecimal price;

  @Enumerated(EnumType.STRING)
  @Column(name = "currency", nullable = false, columnDefinition = "TEXT")
  private Currency currency;

  @ManyToOne(optional = false)
  @JoinColumn(name = "vehicle_id", nullable = false)
  private Vehicle vehicle;

  public VehicleRate() {}

  public UUID getId() {
    return id;
  }

  public Vehicle getVehicle() {
    return vehicle;
  }

  public void setVehicle(Vehicle vehicle) {
    this.vehicle = vehicle;
  }

  public RateType getType() {
    return type;
  }

  public void setType(RateType type) {
    this.type = type;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public void setPrice(BigDecimal price) {
    this.price = price;
  }

  public Currency getCurrency() {
    return currency;
  }

  public void setCurrency(Currency currency) {
    this.currency = currency;
  }
}
