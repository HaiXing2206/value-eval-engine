package com.example.engine.rule;

import com.example.engine.common.ApiResp;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rule")
@RequiredArgsConstructor
public class RuleController {

  private final JdbcTemplate jdbc;
  private final PreprocessRepo preprocessRepo;

  @PostMapping("/qual/{indicatorCode}")
  public ApiResp<Void> upsertQual(
      @PathVariable("indicatorCode") String indicatorCode, @RequestBody List<QualItem> items) {
    for (QualItem it : items) {
      jdbc.update(
          """
          insert into t_indicator_rule_qual(indicator_code, enum_key, score)
          values(?,?,?)
          on duplicate key update score=values(score)
          """,
          indicatorCode,
          it.enumKey,
          it.score);
    }
    return ApiResp.ok(null);
  }

  @PostMapping("/quant/{indicatorCode}")
  public ApiResp<Void> upsertQuant(
      @PathVariable("indicatorCode") String indicatorCode, @RequestBody List<QuantItem> items) {
    jdbc.update("delete from t_indicator_rule_quant where indicator_code=?", indicatorCode);
    for (QuantItem it : items) {
      jdbc.update(
          """
          insert into t_indicator_rule_quant(indicator_code, min_val, max_val, score)
          values(?,?,?,?)
          """,
          indicatorCode,
          it.minVal,
          it.maxVal,
          it.score);
    }
    return ApiResp.ok(null);
  }

  @PostMapping("/preprocess/quant/{indicatorCode}")
  public ApiResp<Void> upsertQuantPreprocess(
      @PathVariable("indicatorCode") String indicatorCode, @RequestBody QuantPreprocessReq req) {
    preprocessRepo.upsertQuant(indicatorCode, req.outlierPolicy, req.minVal, req.maxVal);
    return ApiResp.ok(null);
  }

  @Data
  public static class QualItem {
    public String enumKey;
    public BigDecimal score;
  }

  @Data
  public static class QuantItem {
    public BigDecimal minVal;
    public BigDecimal maxVal;
    public BigDecimal score;
  }

  @Data
  public static class QuantPreprocessReq {
    public String outlierPolicy; // CLAMP/FAIL/MARK
    public BigDecimal minVal;
    public BigDecimal maxVal;
  }
}
