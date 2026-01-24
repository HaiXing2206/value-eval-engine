package com.example.engine.task;

import com.example.engine.caliber.CaliberService;
import com.example.engine.rule.IndicatorRuleService;
import com.example.engine.task.dto.CalcRequest;
import com.example.engine.task.dto.CalcResponse;
import com.example.engine.weight.WeightItem;
import com.example.engine.weight.WeightService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CalcService {

  private final TaskRepo taskRepo;
  private final ResultRepo resultRepo;

  private final CaliberService caliberService;
  private final WeightService weightService;
  private final IndicatorRuleService ruleService;

  private final ObjectMapper om = new ObjectMapper();

  @Transactional
  public CalcResponse calculate(CalcRequest req) {

    var caliber = caliberService.get(req.getCaliberVersionCode());
    if (!"PUBLISHED".equalsIgnoreCase(caliber.getStatus())) {
      throw new IllegalArgumentException("口径版本未发布: " + caliber.getCaliberCode());
    }

    var weightVer = weightService.getByCode(caliber.getWeightVersionCode());
    if (!"PUBLISHED".equalsIgnoreCase(weightVer.getStatus())) {
      throw new IllegalArgumentException("权重版本未发布: " + weightVer.getVersionCode());
    }
    List<WeightItem> wItems = weightService.listItems(weightVer.getId());

    EvalTask task = new EvalTask();
    task.setTaskName(req.getTaskName());
    task.setAssetId(req.getAssetId());
    task.setWeightVersionCode(weightVer.getVersionCode());
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

    for (WeightItem wi : wItems) {
      String code = wi.getIndicatorCode();
      BigDecimal w = wi.getWeight();
      Object raw = req.getValues().get(code);

      BigDecimal score = ruleService.toScore(code, raw);
      BigDecimal contrib = score.multiply(w);

      total = total.add(contrib);

      items.add(
          CalcResponse.Item.builder()
              .code(code)
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
      snapshot.put("weightVersionCode", weightVer.getVersionCode());
      snapshot.put("levelA", caliber.getLevelA());
      snapshot.put("levelB", caliber.getLevelB());
      snapshot.put("levelC", caliber.getLevelC());
      snapshot.put("weights", wItems);
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
}
