package io.stxkxs.model.aws.codebuild;

import io.stxkxs.model.aws.cloudwatch.LogGroupConf;

public record Logging(
  LogGroupConf logGroup,
  String prefix,
  boolean enabled
) {}
