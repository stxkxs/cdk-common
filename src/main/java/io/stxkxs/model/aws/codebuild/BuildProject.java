package io.stxkxs.model.aws.codebuild;

/**
 * CodeBuild project configuration.
 */
public record BuildProject(boolean badge, String name, String description, String buildspec, int concurrentBuildLimit,
  Environment environment, Logging logging, boolean cache) {}
