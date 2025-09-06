package io.stxkxs.model.aws.cloudwatch;

import java.util.List;
import java.util.Map;
import lombok.Builder;

/**
 * CloudWatch alarm configuration.
 */
@Builder
public record AlarmConf(String name, String description, String metricNamespace, String metricName, String statistic,
  Map<String, String> dimensions, Integer periodMinutes, Integer evaluationPeriods, Double threshold, String comparisonOperator,
  String treatMissingData, List<String> alarmActions, Map<String, String> tags) {}
