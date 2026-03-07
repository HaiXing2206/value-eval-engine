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
@Table(name = "t_eval_task")
public class EvalTask {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "task_name", nullable = false, length = 128)
  private String taskName;

  @Column(name = "asset_id", nullable = false, length = 64)
  private String assetId;

  @Column(name = "weight_version_code", nullable = false, length = 64)
  private String weightVersionCode;

  @Column(name = "caliber_version_code", length = 64)
  private String caliberVersionCode;

  @Column(nullable = false, length = 16)
  private String status = "CREATED";

  @Column(name = "input_json", columnDefinition = "json")
  private String inputJson;

  @Column(name = "create_time", insertable = false, updatable = false)
  private LocalDateTime createTime;

  @Column(name = "update_time", insertable = false, updatable = false)
  private LocalDateTime updateTime;
}
