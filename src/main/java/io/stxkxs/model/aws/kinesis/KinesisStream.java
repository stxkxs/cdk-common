package io.stxkxs.model.aws.kinesis;

import io.stxkxs.model.aws.kms.Kms;

public record KinesisStream(
  boolean enabled,
  String name,
  int shards,
  String mode,
  String encryption,
  Kms kms,
  String removalPolicy,
  int retentionPeriod
) {}
