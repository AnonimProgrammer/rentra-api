package com.rentra.domain.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
public class VehicleMedia {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(columnDefinition = "BINARY(16)")
  private UUID id;

  private String imgUrl;
}
