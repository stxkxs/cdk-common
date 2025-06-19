package io.stxkxs.model.aws.loadbalancer;

public record HealthCheck(
  boolean enabled,
  String healthyHttpCodes,
  String path,
  String port,
  String protocol
) {}
