package io.stxkxs.model.aws.cloudwatch;

import java.util.Map;
import lombok.Builder;

@Builder
public record DashboardConf(String name, String body, Map<String, String> tags) {}
