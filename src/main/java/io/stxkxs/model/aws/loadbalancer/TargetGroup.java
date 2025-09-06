package io.stxkxs.model.aws.loadbalancer;

public record TargetGroup(String alpnPolicy, boolean connectionTermination, HealthCheck healthcheck, String name, int port,
  boolean preserveClientIp, String protocol, String type) {}
