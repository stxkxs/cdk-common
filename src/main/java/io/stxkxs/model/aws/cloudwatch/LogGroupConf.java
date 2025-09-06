package io.stxkxs.model.aws.cloudwatch;

import io.stxkxs.model.aws.kms.Kms;
import java.util.Map;
import lombok.Builder;

/**
 * CloudWatch log group configuration.
 */
@Builder
public record LogGroupConf(String name, String type, String retention, Kms kms, String removalPolicy, Map<String, String> tags) {}
