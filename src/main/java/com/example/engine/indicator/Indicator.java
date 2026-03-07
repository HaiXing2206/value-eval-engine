package com.example.engine.indicator;

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
@Table(name = "t_indicator")
public class Indicator {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 64)
  private String code;

  @Column(nullable = false, length = 128)
  private String name;

  @Column(name = "metric_type", nullable = false, length = 16)
  private String metricType; // QUANT/QUAL/RULE

  private String unit;
  private Integer enabled = 1;
  private String remark;

  @Column(name = "create_time", insertable = false, updatable = false)
  private LocalDateTime createTime;

  @Column(name = "update_time", insertable = false, updatable = false)
  private LocalDateTime updateTime;
}
