package com.example.engine.weight;

import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WeightService {

  private final WeightRepo verRepo;
  private final JdbcTemplate jdbc;

  public WeightVersion createVersion(String versionCode, String remark) {
    WeightVersion v = new WeightVersion();
    v.setVersionCode(versionCode);
    v.setRemark(remark);
    return verRepo.save(v);
  }

  public WeightVersion getByCode(String code) {
    return verRepo
        .findByVersionCode(code)
        .orElseThrow(() -> new IllegalArgumentException("权重版本不存在: " + code));
  }

  public List<WeightItem> listItems(Long versionId) {
    return jdbc.query(
        "select id, version_id, indicator_code, weight from t_weight_item where version_id=?",
        (rs, rowNum) -> {
          WeightItem it = new WeightItem();
          it.setId(rs.getLong("id"));
          it.setVersionId(rs.getLong("version_id"));
          it.setIndicatorCode(rs.getString("indicator_code"));
          it.setWeight(rs.getBigDecimal("weight"));
          return it;
        },
        versionId);
  }

  @Transactional
  public void upsertItems(Long versionId, List<WeightItem> items) {
    BigDecimal sum =
        items.stream().map(WeightItem::getWeight).reduce(BigDecimal.ZERO, BigDecimal::add);
    if (sum.subtract(BigDecimal.ONE).abs().compareTo(new BigDecimal("0.000001")) > 0) {
      throw new IllegalArgumentException("权重之和必须为1，当前=" + sum);
    }

    for (WeightItem it : items) {
      jdbc.update(
          """
          insert into t_weight_item(version_id, indicator_code, weight)
          values(?,?,?)
          on duplicate key update weight=values(weight)
          """,
          versionId,
          it.getIndicatorCode(),
          it.getWeight());
    }
  }

  @Transactional
  public void publish(String versionCode) {
    WeightVersion v = getByCode(versionCode);
    v.setStatus("PUBLISHED");
    verRepo.save(v);
  }
}
