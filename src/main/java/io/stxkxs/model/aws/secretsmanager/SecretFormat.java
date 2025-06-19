package io.stxkxs.model.aws.secretsmanager;

public record SecretFormat(
  boolean excludeLowercase,
  boolean excludeNumbers,
  boolean excludeUppercase,
  boolean includeSpace,
  int length,
  boolean requireEachIncludedType
) {}
