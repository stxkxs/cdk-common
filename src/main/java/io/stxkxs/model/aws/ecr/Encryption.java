package io.stxkxs.model.aws.ecr;

import io.stxkxs.model.aws.kms.Kms;

public record Encryption(boolean enabled, Kms kms) {}
