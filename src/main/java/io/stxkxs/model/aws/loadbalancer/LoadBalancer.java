package io.stxkxs.model.aws.loadbalancer;

import java.util.Map;

public record LoadBalancer(String name, TargetGroup defaultTargetGroup, boolean internetFacing, boolean crossZoneEnabled,
  boolean deletionProtection, Map<String, String> tags) {}
