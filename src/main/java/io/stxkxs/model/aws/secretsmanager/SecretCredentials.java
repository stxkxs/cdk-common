package io.stxkxs.model.aws.secretsmanager;

import java.util.Map;

public record SecretCredentials(
  String name,
  String description,
  String username,
  SecretFormat password,
  String removalPolicy,
  Map<String, String> tags
) {}
