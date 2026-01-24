package com.example.engine.rule;

import com.example.engine.indicator.Indicator;
import com.example.engine.indicator.IndicatorRepo;
import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IndicatorRuleService {

  private final IndicatorRepo indicatorRepo;
  private final RuleRepo ruleRepo;

  /**
   * 把原始值转成 0~100 分
   */
  public BigDecimal toScore(String indicatorCode, Object rawVal, String missingPolicy) {
    Indicator ind =
        indicatorRepo
            .findByCode(indicatorCode)
            .orElseThrow(() -> new IllegalArgumentException("指标不存在: " + indicatorCode));

    if (rawVal == null || String.valueOf(rawVal).trim().isEmpty()) {
      return handleMissing(ind, missingPolicy);
    }

    String type = ind.getMetricType(); // QUANT/QUAL/RULE

    return switch (type) {
      case "QUANT" -> scoreQuant(indicatorCode, rawVal);
      case "QUAL" -> scoreQual(indicatorCode, rawVal);
      default -> throw new IllegalArgumentException("暂不支持的指标类型: " + type);
    };
  }

  private BigDecimal handleMissing(Indicator ind, String missingPolicy) {
    String code = ind.getCode();
    String type = ind.getMetricType();

    if ("FAIL".equalsIgnoreCase(missingPolicy)) {
      throw new IllegalArgumentException("存在缺失指标，按口径策略FAIL终止: " + code);
    }
    if ("WORST".equalsIgnoreCase(missingPolicy)) {
      BigDecimal worst = null;
      if ("QUAL".equals(type)) {
        worst = ruleRepo.minQualScore(code);
      }
      if ("QUANT".equals(type)) {
        worst = ruleRepo.minQuantScore(code);
      }
      return worst == null ? BigDecimal.ZERO : clamp100(worst);
    }
    return BigDecimal.ZERO;
  }

  private BigDecimal scoreQual(String indicatorCode, Object rawVal) {
    String key = String.valueOf(rawVal).trim();
    BigDecimal s = ruleRepo.findQualScore(indicatorCode, key);
    return s == null ? BigDecimal.ZERO : clamp100(s);
  }

  private BigDecimal scoreQuant(String indicatorCode, Object rawVal) {
    BigDecimal v;
    try {
      v = new BigDecimal(String.valueOf(rawVal).trim());
    } catch (Exception e) {
      return BigDecimal.ZERO;
    }

    if (v.compareTo(BigDecimal.ONE) > 0 && v.compareTo(new BigDecimal("100")) <= 0) {
      v = v.divide(new BigDecimal("100"), 8, RoundingMode.HALF_UP);
    }

    BigDecimal s = ruleRepo.findQuantScore(indicatorCode, v);
    return s == null ? BigDecimal.ZERO : clamp100(s);
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
}
