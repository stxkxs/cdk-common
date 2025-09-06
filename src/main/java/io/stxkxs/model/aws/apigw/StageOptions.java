package io.stxkxs.model.aws.apigw;

import java.util.Map;

public record StageOptions(String stageName, String description, String loggingLevel, Map<String, String> variables, boolean cachingEnabled,
  boolean dataTraceEnabled, boolean metricsEnabled, boolean tracingEnabled, int throttlingBurstLimit, int throttlingRateLimit) {}
