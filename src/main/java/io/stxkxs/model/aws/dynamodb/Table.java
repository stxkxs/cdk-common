package io.stxkxs.model.aws.dynamodb;

import java.util.List;
import java.util.Map;

public record Table(String name, SortKey partitionKey, SortKey sortKey, List<Index> localSecondaryIndexes,
  List<Index> globalSecondaryIndexes, Encryption encryption, Billing billing, String tableClass, Streams streams,
  boolean contributorInsights, boolean deletionProtection, boolean pointInTimeRecovery, String removalPolicy, Map<String, String> tags) {}
