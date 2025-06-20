package io.stxkxs.model.aws.codebuild;

public record BuildProject(
  boolean badge,
  String name,
  String description,
  String buildspec,
  int concurrentBuildLimit,
  Environment environment,
  Logging logging,
  boolean cache
) {}
