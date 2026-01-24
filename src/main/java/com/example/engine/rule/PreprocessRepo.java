package com.example.engine.rule;

import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PreprocessRepo {

  private final JdbcTemplate jdbc;

  public QuantPreprocess findQuant(String indicatorCode) {
    List<QuantPreprocess> list =
        jdbc.query(
            "select outlier_policy, min_val, max_val from t_indicator_preprocess_quant where indicator_code=?",
            (rs, i) -> {
              QuantPreprocess p = new QuantPreprocess();
              p.outlierPolicy = rs.getString("outlier_policy");
              p.minVal = rs.getBigDecimal("min_val");
              p.maxVal = rs.getBigDecimal("max_val");
              return p;
            },
            indicatorCode);
    return list.isEmpty() ? null : list.get(0);
  }

  public void upsertQuant(String indicatorCode, String policy, BigDecimal minVal, BigDecimal maxVal) {
    jdbc.update(
        """
        insert into t_indicator_preprocess_quant(indicator_code, outlier_policy, min_val, max_val)
        values(?,?,?,?)
        on duplicate key update outlier_policy=values(outlier_policy),
          min_val=values(min_val), max_val=values(max_val)
        """,
        indicatorCode,
        policy,
        minVal,
        maxVal);
  }

  public static class QuantPreprocess {
    public String outlierPolicy; // CLAMP/FAIL/MARK
    public BigDecimal minVal;
    public BigDecimal maxVal;
  }
}
