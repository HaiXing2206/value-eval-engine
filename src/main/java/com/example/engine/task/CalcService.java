package com.example.engine.task;

import com.example.engine.caliber.CaliberService;
import com.example.engine.task.dto.CalcRequest;
import com.example.engine.task.dto.CalcResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CalcService {

  private final TaskRepo taskRepo;
  private final ResultRepo resultRepo;

  private final CaliberService caliberService;

  private final ObjectMapper om = new ObjectMapper();

  @Transactional
  public CalcResponse calculate(CalcRequest req) {

    var caliber = caliberService.get(req.getCaliberVersionCode());
    if (!"PUBLISHED".equalsIgnoreCase(caliber.getStatus())) {
      throw new IllegalArgumentException("口径版本未发布: " + caliber.getCaliberCode());
    }
    if (caliber.getSnapshotJson() == null || caliber.getSnapshotJson().isEmpty()) {
      throw new IllegalArgumentException("口径未生成快照，请重新发布口径: " + caliber.getCaliberCode());
    }

    com.fasterxml.jackson.databind.JsonNode snap;
    try {
      snap = om.readTree(caliber.getSnapshotJson());
    } catch (Exception e) {
      throw new IllegalArgumentException("口径快照解析失败");
    }
    var rulesNode = snap.path("rules");

    String weightVersionCode = snap.path("weightVersionCode").asText(caliber.getWeightVersionCode());

    EvalTask task = new EvalTask();
    task.setTaskName(req.getTaskName());
    task.setAssetId(req.getAssetId());
    task.setWeightVersionCode(weightVersionCode);
    task.setCaliberVersionCode(caliber.getCaliberCode());
    task.setStatus("RUNNING");
    try {
      task.setInputJson(om.writeValueAsString(req.getValues()));
    } catch (Exception e) {
      throw new IllegalArgumentException("输入序列化失败");
    }
    task = taskRepo.save(task);

    BigDecimal total = BigDecimal.ZERO;
    List<CalcResponse.Item> items = new ArrayList<>();

    var weightsNode = snap.path("weights");
    Set<String> weightCodes = new LinkedHashSet<>();
    for (var weightNode : weightsNode) {
      String code = weightNode.path("indicatorCode").asText();
      if (code != null && !code.isBlank()) {
        weightCodes.add(code);
      }
    }
    validateInputKeys(req.getValues(), weightCodes);

    for (var weightNode : weightsNode) {
      String code = weightNode.path("indicatorCode").asText();
      BigDecimal w = new BigDecimal(weightNode.path("weight").asText("0"));
      Object raw = req.getValues().get(code);

      var one = rulesNode.path(code);
      String metricType = one.path("metricType").asText();

      boolean isMissing = (raw == null || String.valueOf(raw).trim().isEmpty());
      boolean isOutlier = false;
      Double processed = null;

      BigDecimal score;

      if (isMissing) {
        score = missingScoreFromPolicy(metricType, one, caliber.getMissingPolicy());
      } else if ("QUAL".equals(metricType)) {
        score = scoreQualFromSnapshot(one, String.valueOf(raw).trim());
      } else if ("QUANT".equals(metricType)) {
        var r = scoreQuantFromSnapshot(one, raw);
        processed = r.processed;
        isOutlier = r.outlier;
        score = r.score;
      } else {
        throw new IllegalArgumentException("不支持的metricType: " + metricType);
      }

      BigDecimal contrib = score.multiply(w);
      total = total.add(contrib);

      items.add(
          CalcResponse.Item.builder()
              .code(code)
              .raw(raw)
              .processed(processed)
              .outlier(isOutlier)
              .missing(isMissing)
              .score(score.doubleValue())
              .weight(w.doubleValue())
              .contribution(contrib.doubleValue())
              .build());
    }

    String level = levelByScore(total, caliber);

    EvalResult r = new EvalResult();
    r.setTaskId(task.getId());
    r.setTotalScore(total.setScale(4, RoundingMode.HALF_UP));
    r.setLevel(level);

    try {
      r.setDetailJson(om.writeValueAsString(items));
      Map<String, Object> snapshot = new LinkedHashMap<>();
      snapshot.put("caliberCode", caliber.getCaliberCode());
      snapshot.put("weightVersionCode", weightVersionCode);
      snapshot.put("levelA", caliber.getLevelA());
      snapshot.put("levelB", caliber.getLevelB());
      snapshot.put("levelC", caliber.getLevelC());
      snapshot.put("weights", weightsNode);
      r.setSnapshotJson(om.writeValueAsString(snapshot));
    } catch (Exception e) {
      throw new IllegalArgumentException("结果序列化失败");
    }

    resultRepo.save(r);

    task.setStatus("DONE");
    taskRepo.save(task);

    return CalcResponse.builder()
        .taskId(task.getId())
        .totalScore(total.doubleValue())
        .level(level)
        .items(items)
        .build();
  }

  private String levelByScore(BigDecimal s, com.example.engine.caliber.CaliberVersion c) {
    if (s.compareTo(c.getLevelA()) >= 0) {
      return "A";
    }
    if (s.compareTo(c.getLevelB()) >= 0) {
      return "B";
    }
    if (s.compareTo(c.getLevelC()) >= 0) {
      return "C";
    }
    return "D";
  }

  private BigDecimal missingScoreFromPolicy(
      String metricType, com.fasterxml.jackson.databind.JsonNode one, String policy) {
    if ("FAIL".equalsIgnoreCase(policy)) {
      throw new IllegalArgumentException("存在缺失指标，口径策略FAIL终止");
    }
    if ("WORST".equalsIgnoreCase(policy)) {
      if ("QUAL".equals(metricType)) {
        BigDecimal min = null;
        for (var n : one.path("qualMap")) {
          BigDecimal s = new BigDecimal(n.path("score").asText("0"));
          min = (min == null || s.compareTo(min) < 0) ? s : min;
        }
        return min == null ? BigDecimal.ZERO : clamp100(min);
      }
      if ("QUANT".equals(metricType)) {
        BigDecimal min = null;
        for (var n : one.path("quantIntervals")) {
          BigDecimal s = new BigDecimal(n.path("score").asText("0"));
          min = (min == null || s.compareTo(min) < 0) ? s : min;
        }
        return min == null ? BigDecimal.ZERO : clamp100(min);
      }
    }
    return BigDecimal.ZERO;
  }

  private BigDecimal scoreQualFromSnapshot(com.fasterxml.jackson.databind.JsonNode one, String key) {
    List<String> enums = new ArrayList<>();
    for (var n : one.path("qualMap")) {
      String enumKey = n.path("enumKey").asText();
      enums.add(enumKey);
      if (key.equals(n.path("enumKey").asText())) {
        return clamp100(new BigDecimal(n.path("score").asText("0")));
      }
    }
    throw new IllegalArgumentException("定性值未命中映射: " + key + "，可选值=" + enums);
  }

  private QuantScoreResult scoreQuantFromSnapshot(
      com.fasterxml.jackson.databind.JsonNode one, Object raw) {
    BigDecimal v;
    try {
      v = new BigDecimal(String.valueOf(raw).trim());
    } catch (Exception e) {
      throw new IllegalArgumentException("定量值不是合法数字: " + raw);
    }

    if (v.compareTo(BigDecimal.ONE) > 0 && v.compareTo(new BigDecimal("100")) <= 0) {
      v = v.divide(new BigDecimal("100"), 8, RoundingMode.HALF_UP);
    }

    var prep = one.path("preprocess");
    String outPolicy = prep.path("outlierPolicy").asText("CLAMP");
    BigDecimal minVal = new BigDecimal(prep.path("minVal").asText("0"));
    BigDecimal maxVal = new BigDecimal(prep.path("maxVal").asText("1.00000001"));

    boolean outlier = (v.compareTo(minVal) < 0 || v.compareTo(maxVal) > 0);

    if (outlier) {
      if ("FAIL".equalsIgnoreCase(outPolicy)) {
        throw new IllegalArgumentException("出现异常值，口径策略FAIL终止，value=" + v);
      }
      if ("CLAMP".equalsIgnoreCase(outPolicy)) {
        if (v.compareTo(minVal) < 0) {
          v = minVal;
        }
        if (v.compareTo(maxVal) > 0) {
          v = maxVal;
        }
        outlier = false;
      }
    }

    for (var n : one.path("quantIntervals")) {
      BigDecimal min = new BigDecimal(n.path("minVal").asText("0"));
      BigDecimal max = new BigDecimal(n.path("maxVal").asText("0"));
      if (v.compareTo(min) >= 0 && v.compareTo(max) < 0) {
        BigDecimal s = clamp100(new BigDecimal(n.path("score").asText("0")));
        return new QuantScoreResult(v.doubleValue(), outlier, s);
      }
    }
    throw new IllegalArgumentException("定量值未落入评分区间: " + v);
  }

  private void validateInputKeys(Map<String, Object> values, Set<String> weightCodes) {
    if (values == null || values.isEmpty()) {
      return;
    }
    List<String> unknown = new ArrayList<>();
    for (String key : values.keySet()) {
      if (key == null || key.isBlank()) {
        continue;
      }
      if (!weightCodes.contains(key)) {
        unknown.add(key);
      }
    }
    if (!unknown.isEmpty()) {
      throw new IllegalArgumentException("输入包含未配置权重的指标: " + unknown);
    }
  }

  private BigDecimal clamp100(BigDecimal s) {
    if (s.compareTo(BigDecimal.ZERO) < 0) {
      return BigDecimal.ZERO;
    }
    if (s.compareTo(new BigDecimal("100")) > 0) {
      return new BigDecimal("100");
    }
    return s;
  }

  private static class QuantScoreResult {
    private final Double processed;
    private final boolean outlier;
    private final BigDecimal score;

    private QuantScoreResult(Double processed, boolean outlier, BigDecimal score) {
      this.processed = processed;
      this.outlier = outlier;
      this.score = score;
    }
  }
}
