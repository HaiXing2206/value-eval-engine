package com.example.engine.caliber;

import com.example.engine.indicator.IndicatorRepo;
import com.example.engine.rule.PreprocessRepo;
import com.example.engine.rule.RuleRepo;
import com.example.engine.weight.WeightService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CaliberService {

  private final CaliberRepo repo;
  private final WeightService weightService;
  private final RuleRepo ruleRepo;
  private final PreprocessRepo preprocessRepo;
  private final IndicatorRepo indicatorRepo;
  private final ObjectMapper om = new ObjectMapper();

  public CaliberVersion get(String code) {
    return repo
        .findByCaliberCode(code)
        .orElseThrow(() -> new IllegalArgumentException("口径版本不存在: " + code));
  }

  public CaliberVersion create(String caliberCode, String weightVersionCode, String remark) {
    CaliberVersion v = new CaliberVersion();
    v.setCaliberCode(caliberCode);
    v.setWeightVersionCode(weightVersionCode);
    v.setRuleRemark(remark);
    v.setLevelA(new BigDecimal("90.0"));
    v.setLevelB(new BigDecimal("75.0"));
    v.setLevelC(new BigDecimal("60.0"));
    return repo.save(v);
  }

  @Transactional
  public void publish(String caliberCode) {
    CaliberVersion v = get(caliberCode);
    if ("PUBLISHED".equalsIgnoreCase(v.getStatus())) {
      return;
    }

    var wv = weightService.getByCode(v.getWeightVersionCode());
    if (!"PUBLISHED".equalsIgnoreCase(wv.getStatus())) {
      throw new IllegalArgumentException("权重版本未发布，不能发布口径: " + wv.getVersionCode());
    }
    var wItems = weightService.listItems(wv.getId());

    Map<String, Object> snap = new LinkedHashMap<>();
    snap.put("caliberCode", v.getCaliberCode());
    snap.put("weightVersionCode", wv.getVersionCode());
    snap.put("missingPolicy", v.getMissingPolicy());
    snap.put("levelA", v.getLevelA());
    snap.put("levelB", v.getLevelB());
    snap.put("levelC", v.getLevelC());

    Map<String, Object> rulePack = new LinkedHashMap<>();
    for (var wi : wItems) {
      String code = wi.getIndicatorCode();
      var ind =
          indicatorRepo
              .findByCode(code)
              .orElseThrow(() -> new IllegalArgumentException("指标不存在: " + code));

      Map<String, Object> one = new LinkedHashMap<>();
      one.put("metricType", ind.getMetricType());

      if ("QUAL".equals(ind.getMetricType())) {
        one.put("qualMap", ruleRepo.listQual(code));
      } else if ("QUANT".equals(ind.getMetricType())) {
        one.put("quantIntervals", ruleRepo.listQuant(code));
        var pp = preprocessRepo.findQuant(code);
        Map<String, Object> prep = new LinkedHashMap<>();
        prep.put("outlierPolicy", pp == null ? "CLAMP" : pp.outlierPolicy);
        prep.put("minVal", pp == null ? new BigDecimal("0") : pp.minVal);
        prep.put("maxVal", pp == null ? new BigDecimal("1.00000001") : pp.maxVal);
        one.put("preprocess", prep);
      }
      rulePack.put(code, one);
    }
    snap.put("rules", rulePack);
    snap.put("weights", wItems);

    try {
      v.setSnapshotJson(om.writeValueAsString(snap));
    } catch (Exception e) {
      throw new IllegalArgumentException("口径快照序列化失败");
    }

    v.setStatus("PUBLISHED");
    repo.save(v);
  }

  @Transactional
  public CaliberVersion copyAsDraft(String fromCode, String newCode, String remark) {
    CaliberVersion src = get(fromCode);

    CaliberVersion v = new CaliberVersion();
    v.setCaliberCode(newCode);
    v.setWeightVersionCode(src.getWeightVersionCode());
    v.setRuleRemark(remark == null ? ("copy-from-" + fromCode) : remark);
    v.setLevelA(src.getLevelA());
    v.setLevelB(src.getLevelB());
    v.setLevelC(src.getLevelC());
    v.setMissingPolicy(src.getMissingPolicy());
    v.setStatus("DRAFT");
    v.setSnapshotJson(null);

    return repo.save(v);
  }
}
