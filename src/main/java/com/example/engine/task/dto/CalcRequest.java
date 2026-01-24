package com.example.engine.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import lombok.Data;

@Data
public class CalcRequest {
  @NotBlank
  private String taskName;

  @NotBlank
  private String assetId;

  @NotBlank
  private String weightVersionCode;

  /**
   * values: 指标编码 -> 分值(0~100) 或者你后续扩展为原始值
   */
  @NotNull
  private Map<String, Double> values;
}
