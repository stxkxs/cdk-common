package io.stxkxs.model.aws.cloudwatch;

import io.stxkxs.model.aws.kms.Kms;
import lombok.Builder;

import java.util.Map;

@Builder
public record LogGroupConf(
  String name,
  String type,
  String retention,
  Kms kms,
  String removalPolicy,
  Map<String, String> tags
) {}
