package io.stxkxs.model.aws.dynamodb;

import software.amazon.awscdk.services.dynamodb.AttributeType;

public record SortKey(
  String name,
  AttributeType type
) {}
