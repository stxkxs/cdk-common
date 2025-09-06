package io.stxkxs.model.aws.ecr;

import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.services.ecr.TagMutability;

public record EcrRepository(String name, boolean scanOnPush, boolean emptyOnDelete, TagMutability tagMutability,
  RemovalPolicy removalPolicy, Encryption encryption) {}
