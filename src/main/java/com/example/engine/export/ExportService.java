package com.example.engine.export;

import com.example.engine.task.EvalResult;
import com.example.engine.task.EvalTask;
import com.example.engine.task.ResultRepo;
import com.example.engine.task.TaskRepo;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExportService {

  private final TaskRepo taskRepo;
  private final ResultRepo resultRepo;
  private final ObjectMapper om = new ObjectMapper();

  public Map<String, Object> exportAsMap(Long taskId, String type) {
    EvalTask task =
        taskRepo.findById(taskId).orElseThrow(() -> new IllegalArgumentException("任务不存在"));
    EvalResult result =
        resultRepo.findByTaskId(taskId).orElseThrow(() -> new IllegalArgumentException("结果不存在"));

    Map<String, Object> out = new LinkedHashMap<>();
    out.put("taskId", task.getId());
    out.put("taskName", task.getTaskName());
    out.put("assetId", task.getAssetId());
    out.put("caliberVersionCode", task.getCaliberVersionCode());
    out.put("weightVersionCode", task.getWeightVersionCode());
    out.put("status", task.getStatus());
    out.put("totalScore", result.getTotalScore());
    out.put("level", result.getLevel());
    out.put("createTime", result.getCreateTime());

    try {
      if ("summary".equalsIgnoreCase(type)) {
        return out;
      }
      if ("detail".equalsIgnoreCase(type)) {
        out.put("detail", om.readTree(result.getDetailJson()));
        return out;
      }
      if ("snapshot".equalsIgnoreCase(type)) {
        out.put("snapshot", om.readTree(result.getSnapshotJson()));
        return out;
      }
    } catch (Exception e) {
      throw new IllegalArgumentException("JSON解析失败");
    }

    throw new IllegalArgumentException("不支持的type: " + type + "（summary/detail/snapshot）");
  }

  public String exportAsCsv(Long taskId, String type) {
    EvalTask task =
        taskRepo.findById(taskId).orElseThrow(() -> new IllegalArgumentException("任务不存在"));
    EvalResult result =
        resultRepo.findByTaskId(taskId).orElseThrow(() -> new IllegalArgumentException("结果不存在"));

    if ("summary".equalsIgnoreCase(type)) {
      return "taskId,taskName,assetId,caliberVersionCode,weightVersionCode,totalScore,level,status\n"
          + task.getId()
          + ","
          + esc(task.getTaskName())
          + ","
          + esc(task.getAssetId())
          + ","
          + esc(task.getCaliberVersionCode())
          + ","
          + esc(task.getWeightVersionCode())
          + ","
          + result.getTotalScore()
          + ","
          + result.getLevel()
          + ","
          + task.getStatus()
          + "\n";
    }

    if ("detail".equalsIgnoreCase(type)) {
      StringBuilder sb = new StringBuilder();
      sb.append("taskId,indicatorCode,raw,processed,outlier,missing,score,weight,contribution\n");
      try {
        var arr = om.readTree(result.getDetailJson());
        for (var n : arr) {
          sb.append(task.getId())
              .append(",")
              .append(esc(n.path("code").asText()))
              .append(",")
              .append(esc(n.path("raw").toString()))
              .append(",")
              .append(n.path("processed").isMissingNode() ? "" : n.path("processed").asText())
              .append(",")
              .append(n.path("outlier").asBoolean(false))
              .append(",")
              .append(n.path("missing").asBoolean(false))
              .append(",")
              .append(n.path("score").asDouble())
              .append(",")
              .append(n.path("weight").asDouble())
              .append(",")
              .append(n.path("contribution").asDouble())
              .append("\n");
        }
      } catch (Exception e) {
        throw new IllegalArgumentException("detail_json解析失败");
      }
      return sb.toString();
    }

    if ("snapshot".equalsIgnoreCase(type)) {
      StringBuilder sb = new StringBuilder();
      sb.append("key,value\n");
      try {
        var snap = om.readTree(result.getSnapshotJson());
        Iterator<String> it = snap.fieldNames();
        while (it.hasNext()) {
          String k = it.next();
          sb.append(esc(k)).append(",").append(esc(snap.get(k).toString())).append("\n");
        }
      } catch (Exception e) {
        throw new IllegalArgumentException("snapshot_json解析失败");
      }
      return sb.toString();
    }

    throw new IllegalArgumentException("不支持的type: " + type);
  }

  private String esc(String s) {
    if (s == null) {
      return "";
    }
    String x = s.replace("\"", "\"\"");
    if (x.contains(",") || x.contains("\n") || x.contains("\r")) {
      return "\"" + x + "\"";
    }
    return x;
  }
}
