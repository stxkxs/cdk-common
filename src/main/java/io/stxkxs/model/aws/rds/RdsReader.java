package io.stxkxs.model.aws.rds;

public record RdsReader(boolean allowMajorVersionUpgrade, boolean autoMinorVersionUpgrade, String name, boolean publiclyAccessible,
  boolean scaleWithWriter, RdsPerformanceInsights performanceInsights) {}
