package com.example.engine.task;

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
@Table(name = "t_eval_result")
public class EvalResult {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "task_id", nullable = false, unique = true)
  private Long taskId;

  @Column(name = "total_score", nullable = false, precision = 18, scale = 4)
  private java.math.BigDecimal totalScore;

  @Column(nullable = false, length = 8)
  private String level;

  @Column(name = "detail_json", columnDefinition = "json")
  private String detailJson;

  @Column(name = "snapshot_json", columnDefinition = "json")
  private String snapshotJson;

  @Column(name = "create_time", updatable = false)
  private LocalDateTime createTime;
}
