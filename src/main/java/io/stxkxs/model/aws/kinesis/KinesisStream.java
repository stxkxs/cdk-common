package io.stxkxs.model.aws.kinesis;

import io.stxkxs.model.aws.kms.Kms;

/**
 * Kinesis stream configuration.
 */
public record KinesisStream(boolean enabled, String name, int shards, String mode, String encryption, Kms kms, String removalPolicy,
  int retentionPeriod) {}
