package com.example.engine.weight;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
    name = "t_weight_item",
    uniqueConstraints =
        @UniqueConstraint(name = "uk_ver_indicator", columnNames = {"version_id", "indicator_code"}))
public class WeightItem {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "version_id", nullable = false)
  private Long versionId;

  @Column(name = "indicator_code", nullable = false, length = 64)
  private String indicatorCode;

  @Column(nullable = false, precision = 18, scale = 8)
  private java.math.BigDecimal weight;
}
