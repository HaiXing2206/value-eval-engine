package com.example.engine.weight;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "t_weight_version")
public class WeightVersion {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "version_code", nullable = false, unique = true, length = 64)
  private String versionCode;

  @Column(nullable = false, length = 16)
  private String status = "DRAFT"; // DRAFT/PUBLISHED

  private String remark;

  @Column(name = "create_time", updatable = false)
  private LocalDateTime createTime;
}
