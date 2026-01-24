package com.example.engine.task.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CalcResponse {
  private Long taskId;
  private double totalScore;
  private String level;
  private List<Item> items;

  @Data
  @Builder
  public static class Item {
    private String code;
    private Object raw;
    private Double processed;
    private Boolean outlier;
    private Boolean missing;
    private double score;
    private double weight;
    private double contribution;
  }
}
