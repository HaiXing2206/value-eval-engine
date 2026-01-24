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

  /**
   * 推荐：传口径版本号（更“软著口径版本化”）
   */
  @NotBlank
  private String caliberVersionCode;

  /**
   * 指标编码 -> 原始值（数字/字符串）
   * QUANT: 0.92 / 92 / 0.8
   * QUAL : "高" / "中" / "低"
   */
  @NotNull
  private Map<String, Object> values;
}
