package com.example.engine.weight.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WeightVersionRequest {
  @NotBlank
  private String versionCode;

  private String remark;
}
