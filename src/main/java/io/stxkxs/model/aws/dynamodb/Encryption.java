package io.stxkxs.model.aws.dynamodb;

import io.stxkxs.model.aws.kms.Kms;

public record Encryption(boolean enabled, String owner, Kms kms) {}
