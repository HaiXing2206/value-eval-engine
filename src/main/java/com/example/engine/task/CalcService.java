package com.example.engine.task;

import com.example.engine.task.dto.CalcRequest;
import com.example.engine.task.dto.CalcResponse;
import com.example.engine.weight.WeightItem;
import com.example.engine.weight.WeightService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CalcService {

  private final TaskRepo taskRepo;
  private final ResultRepo resultRepo;
  private final WeightService weightService;
  private final ObjectMapper om = new ObjectMapper();

  @Transactional
  public CalcResponse calculate(CalcRequest req) {
    var ver = weightService.getByCode(req.getWeightVersionCode());
    if (!"PUBLISHED".equalsIgnoreCase(ver.getStatus())) {
      throw new IllegalArgumentException("权重版本未发布: " + ver.getVersionCode());
    }

    EvalTask task = new EvalTask();
    task.setTaskName(req.getTaskName());
    task.setAssetId(req.getAssetId());
    task.setWeightVersionCode(req.getWeightVersionCode());
    task.setStatus("RUNNING");
    try {
      task.setInputJson(om.writeValueAsString(req.getValues()));
    } catch (Exception e) {
      throw new IllegalArgumentException("输入序列化失败");
    }
    task = taskRepo.save(task);

    List<WeightItem> wItems = weightService.listItems(ver.getId());

    double total = 0.0;
    List<CalcResponse.Item> items = new ArrayList<>();

    for (WeightItem wi : wItems) {
      double w = wi.getWeight().doubleValue();
      double s = Optional.ofNullable(req.getValues().get(wi.getIndicatorCode())).orElse(0.0);
      if (s < 0) {
        s = 0;
      }
      if (s > 100) {
        s = 100;
      }

      double c = s * w;
      total += c;

      items.add(
          CalcResponse.Item.builder()
              .code(wi.getIndicatorCode())
              .score(s)
              .weight(w)
              .contribution(c)
              .build());
    }

    String level = levelByScore(total);

    EvalResult r = new EvalResult();
    r.setTaskId(task.getId());
    r.setTotalScore(new BigDecimal(String.format(Locale.ROOT, "%.4f", total)));
    r.setLevel(level);
    try {
      r.setDetailJson(om.writeValueAsString(items));
      Map<String, Object> snapshot = new HashMap<>();
      snapshot.put("weightVersionCode", ver.getVersionCode());
      snapshot.put("weightVersionId", ver.getId());
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
        .totalScore(total)
        .level(level)
        .items(items)
        .build();
  }

  private String levelByScore(double s) {
    if (s >= 90) {
      return "A";
    }
    if (s >= 75) {
      return "B";
    }
    if (s >= 60) {
      return "C";
    }
    return "D";
  }
}
