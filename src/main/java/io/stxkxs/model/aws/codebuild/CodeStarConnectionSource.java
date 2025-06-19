package io.stxkxs.model.aws.codebuild;

public record CodeStarConnectionSource(
  String owner,
  String repo,
  String branch,
  String connection,
  boolean triggerOnPush
) {}
