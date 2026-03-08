package com.example.engine.weight;

import com.example.engine.system.IndicatorSystemNode;
import com.example.engine.system.IndicatorSystemNodeRepo;
import com.example.engine.system.IndicatorSystemVersion;
import com.example.engine.system.IndicatorSystemVersionRepo;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WeightService {

  private final WeightRepo verRepo;
  private final JdbcTemplate jdbc;
  private final IndicatorSystemVersionRepo systemVersionRepo;
  private final IndicatorSystemNodeRepo systemNodeRepo;

  public WeightVersion createVersion(String versionCode, String systemVersionCode, String remark) {
    String verCode = trimToNull(versionCode);
    String sysCode = trimToNull(systemVersionCode);
    if (verCode == null) {
      throw new IllegalArgumentException("versionCode不能为空");
    }
    if (sysCode == null) {
      throw new IllegalArgumentException("systemVersionCode不能为空");
    }
    ensurePublishedSystemVersion(sysCode);

    WeightVersion v = new WeightVersion();
    v.setVersionCode(verCode);
    v.setSystemVersionCode(sysCode);
    v.setRemark(trimToNull(remark));
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
    WeightVersion v =
        verRepo
            .findById(versionId)
            .orElseThrow(() -> new IllegalArgumentException("权重版本不存在: id=" + versionId));
    ensureDraft(v);

    Set<String> allowedIndicators = loadAllowedIndicators(v);
    BigDecimal sum =
        items.stream().map(WeightItem::getWeight).reduce(BigDecimal.ZERO, BigDecimal::add);
    if (sum.subtract(BigDecimal.ONE).abs().compareTo(new BigDecimal("0.000001")) > 0) {
      throw new IllegalArgumentException("权重之和必须为1，当前=" + sum);
    }

    for (WeightItem it : items) {
      String indicatorCode = trimToNull(it.getIndicatorCode());
      if (indicatorCode == null) {
        throw new IllegalArgumentException("indicatorCode不能为空");
      }
      if (!allowedIndicators.contains(indicatorCode)) {
        throw new IllegalArgumentException(
            "indicatorCode不属于绑定体系版本: " + indicatorCode + "，systemVersionCode=" + v.getSystemVersionCode());
      }
      jdbc.update(
          """
          insert into t_weight_item(version_id, indicator_code, weight)
          values(?,?,?)
          on duplicate key update weight=values(weight)
          """,
          versionId,
          indicatorCode,
          it.getWeight());
    }
  }

  @Transactional
  public void publish(String versionCode) {
    WeightVersion v = getByCode(versionCode);
    ensureDraft(v);
    Set<String> allowedIndicators = loadAllowedIndicators(v);
    List<WeightItem> items = listItems(v.getId());
    if (items.isEmpty()) {
      throw new IllegalArgumentException("发布失败：权重项为空");
    }
    for (WeightItem it : items) {
      if (!allowedIndicators.contains(it.getIndicatorCode())) {
        throw new IllegalArgumentException(
            "发布失败：存在不属于绑定体系版本的指标: "
                + it.getIndicatorCode()
                + "，systemVersionCode="
                + v.getSystemVersionCode());
      }
    }
    v.setStatus("PUBLISHED");
    verRepo.save(v);
  }

  private Set<String> loadAllowedIndicators(WeightVersion weightVersion) {
    String systemVersionCode = trimToNull(weightVersion.getSystemVersionCode());
    if (systemVersionCode == null) {
      throw new IllegalArgumentException("权重版本未绑定体系版本: " + weightVersion.getVersionCode());
    }
    IndicatorSystemVersion systemVersion = ensurePublishedSystemVersion(systemVersionCode);
    List<IndicatorSystemNode> nodes = systemNodeRepo.findByVersionIdOrderBySortNoAscIdAsc(systemVersion.getId());
    Set<String> indicators = new LinkedHashSet<>();
    for (IndicatorSystemNode node : nodes) {
      String indicatorCode = trimToNull(node.getIndicatorCode());
      if (indicatorCode != null) {
        indicators.add(indicatorCode);
      }
    }
    if (indicators.isEmpty()) {
      throw new IllegalArgumentException("绑定体系版本没有可用指标: " + systemVersionCode);
    }
    return indicators;
  }

  private IndicatorSystemVersion ensurePublishedSystemVersion(String systemVersionCode) {
    IndicatorSystemVersion systemVersion =
        systemVersionRepo
            .findByVersionCode(systemVersionCode)
            .orElseThrow(() -> new IllegalArgumentException("指标体系版本不存在: " + systemVersionCode));
    if (!"PUBLISHED".equalsIgnoreCase(systemVersion.getStatus())) {
      throw new IllegalArgumentException("指标体系版本未发布: " + systemVersionCode);
    }
    return systemVersion;
  }

  private void ensureDraft(WeightVersion v) {
    if (!"DRAFT".equalsIgnoreCase(v.getStatus())) {
      throw new IllegalArgumentException("当前权重版本已发布，不可修改: " + v.getVersionCode());
    }
  }

  private String trimToNull(String s) {
    if (s == null) {
      return null;
    }
    String t = s.trim();
    return t.isEmpty() ? null : t;
  }
}
