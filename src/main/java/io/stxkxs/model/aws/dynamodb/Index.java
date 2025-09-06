package io.stxkxs.model.aws.dynamodb;

import java.util.List;
import software.amazon.awscdk.services.dynamodb.ProjectionType;

public record Index(String name, SortKey sortKey, ProjectionType projectionType, List<String> nonKeyAttributes) {}
