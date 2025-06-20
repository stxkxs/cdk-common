package io.stxkxs.model.aws.dynamodb;

import io.stxkxs.model.aws.kinesis.KinesisStream;

public record Streams(
  KinesisStream kinesis,
  DynamoDbStream dynamoDb
) {}
