package com.example.engine.caliber;

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
@Table(name = "t_caliber_version")
public class CaliberVersion {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "caliber_code", nullable = false, unique = true, length = 64)
  private String caliberCode;

  @Column(name = "weight_version_code", nullable = false, length = 64)
  private String weightVersionCode;

  @Column(name = "rule_remark")
  private String ruleRemark;

  @Column(name = "level_a", nullable = false)
  private java.math.BigDecimal levelA;

  @Column(name = "level_b", nullable = false)
  private java.math.BigDecimal levelB;

  @Column(name = "level_c", nullable = false)
  private java.math.BigDecimal levelC;

  @Column(nullable = false, length = 16)
  private String status = "DRAFT";

  @Column(name = "missing_policy", nullable = false, length = 16)
  private String missingPolicy = "ZERO";

  @Column(name = "snapshot_json", columnDefinition = "json")
  private String snapshotJson;

  @Column(name = "create_time", updatable = false)
  private LocalDateTime createTime;
}
