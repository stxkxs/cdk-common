package io.stxkxs.model.aws.kms;

/**
 * KMS key configuration.
 */
public record Kms(String alias, String description, boolean enabled, boolean enableKeyRotation, String keyUsage, String keySpec,
  String removalPolicy) {}
