package io.stxkxs.model.aws.codebuild;

import io.stxkxs.model.aws.cloudwatch.LogGroupConf;

/**
 * Logging configuration for CodeBuild projects.
 */
public record Logging(LogGroupConf logGroup, String prefix, boolean enabled) {}
