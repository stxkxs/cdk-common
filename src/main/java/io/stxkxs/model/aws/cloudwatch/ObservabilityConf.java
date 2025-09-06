package io.stxkxs.model.aws.cloudwatch;

import java.util.List;
import java.util.Map;
import lombok.Builder;

@Builder
public record ObservabilityConf(Map<String, List<String>> topics, List<MetricFilterConf> metrics, List<AlarmConf> alarms,
  List<DashboardConf> dashboards) {}
