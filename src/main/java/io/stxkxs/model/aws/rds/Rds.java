package io.stxkxs.model.aws.rds;

import io.stxkxs.model.aws.secretsmanager.SecretCredentials;
import java.util.List;
import java.util.Map;

public record Rds(String version, String name, String databaseName, SecretCredentials credentials, String storageType,
  boolean enableDataApi, RdsWriter writer, List<RdsReader> readers, boolean deletionProtection, String removalPolicy,
  Map<String, String> tags) {}
