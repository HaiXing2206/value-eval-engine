package com.example.engine.task;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TaskQueryRepo {

  private final JdbcTemplate jdbc;

  public List<EvalTask> findLatest(String assetId, String caliberVersionCode, int limit) {
    StringBuilder sql =
        new StringBuilder(
            """
      select id, task_name, asset_id, weight_version_code, caliber_version_code, status, input_json, create_time, update_time
      from t_eval_task where 1=1
    """);
    List<Object> args = new ArrayList<>();

    if (assetId != null && !assetId.isBlank()) {
      sql.append(" and asset_id=?");
      args.add(assetId.trim());
    }
    if (caliberVersionCode != null && !caliberVersionCode.isBlank()) {
      sql.append(" and caliber_version_code=?");
      args.add(caliberVersionCode.trim());
    }
    sql.append(" order by id desc limit ?");
    args.add(Math.min(Math.max(limit, 1), 200));

    return jdbc.query(
        sql.toString(),
        (rs, i) -> {
          EvalTask t = new EvalTask();
          t.setId(rs.getLong("id"));
          t.setTaskName(rs.getString("task_name"));
          t.setAssetId(rs.getString("asset_id"));
          t.setWeightVersionCode(rs.getString("weight_version_code"));
          t.setCaliberVersionCode(rs.getString("caliber_version_code"));
          t.setStatus(rs.getString("status"));
          t.setInputJson(rs.getString("input_json"));
          Timestamp ct = rs.getTimestamp("create_time");
          Timestamp ut = rs.getTimestamp("update_time");
          t.setCreateTime(ct == null ? null : ct.toLocalDateTime());
          t.setUpdateTime(ut == null ? null : ut.toLocalDateTime());
          return t;
        },
        args.toArray());
  }
}
