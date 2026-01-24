package com.example.engine.rule;

import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RuleRepo {

  private final JdbcTemplate jdbc;

  public BigDecimal findQualScore(String indicatorCode, String enumKey) {
    List<BigDecimal> list =
        jdbc.query(
            "select score from t_indicator_rule_qual where indicator_code=? and enum_key=?",
            (rs, i) -> rs.getBigDecimal("score"),
            indicatorCode,
            enumKey);
    return list.isEmpty() ? null : list.get(0);
  }

  public BigDecimal findQuantScore(String indicatorCode, BigDecimal v) {
    List<BigDecimal> list =
        jdbc.query(
            """
        select score from t_indicator_rule_quant
        where indicator_code=? and ? >= min_val and ? < max_val
        order by min_val asc limit 1
        """,
            (rs, i) -> rs.getBigDecimal("score"),
            indicatorCode,
            v,
            v);
    return list.isEmpty() ? null : list.get(0);
  }

  public BigDecimal minQualScore(String indicatorCode) {
    List<BigDecimal> list =
        jdbc.query(
            "select min(score) as score from t_indicator_rule_qual where indicator_code=?",
            (rs, i) -> rs.getBigDecimal("score"),
            indicatorCode);
    return list.isEmpty() ? null : list.get(0);
  }

  public BigDecimal minQuantScore(String indicatorCode) {
    List<BigDecimal> list =
        jdbc.query(
            "select min(score) as score from t_indicator_rule_quant where indicator_code=?",
            (rs, i) -> rs.getBigDecimal("score"),
            indicatorCode);
    return list.isEmpty() ? null : list.get(0);
  }
}
