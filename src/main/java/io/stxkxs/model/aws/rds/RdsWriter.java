package io.stxkxs.model.aws.rds;

public record RdsWriter(
  boolean allowMajorVersionUpgrade,
  boolean autoMinorVersionUpgrade,
  String name,
  boolean publiclyAccessible,
  RdsPerformanceInsights performanceInsights
) {}
