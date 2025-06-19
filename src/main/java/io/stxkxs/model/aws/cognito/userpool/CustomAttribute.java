package io.stxkxs.model.aws.cognito.userpool;

public record CustomAttribute(
  String name,
  String type,
  int min,
  int max,
  boolean mutable
) {}
