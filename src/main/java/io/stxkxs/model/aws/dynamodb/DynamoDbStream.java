package io.stxkxs.model.aws.dynamodb;

public record DynamoDbStream(
  boolean enabled,
  String type
) {}
