package io.stxkxs.model.aws.dynamodb;

public record Provisioned(
  int min,
  int max,
  int seed,
  int targetUtilizationPercent
) {}
