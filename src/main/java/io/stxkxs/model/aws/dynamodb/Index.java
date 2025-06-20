package io.stxkxs.model.aws.dynamodb;

import software.amazon.awscdk.services.dynamodb.ProjectionType;

import java.util.List;

public record Index(
  String name,
  SortKey sortKey,
  ProjectionType projectionType,
  List<String> nonKeyAttributes
) {}
