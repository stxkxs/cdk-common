package io.stxkxs.model.aws.codebuild;

/**
 * CodeStar connection source configuration for pipelines.
 */
public record CodeStarConnectionSource(String owner, String repo, String branch, String connection, boolean triggerOnPush) {}
