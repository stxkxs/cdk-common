package io.stxkxs.model.aws.ses;

import java.util.Map;

public record ConfigurationSetConf(String name, String customTrackingRedirectDomain, boolean reputationMetrics, boolean sendingEnabled,
  String tlsPolicyConfiguration, String suppressionReasons, DedicatedIpPool dedicatedIpPool, Map<String, String> tags) {}
