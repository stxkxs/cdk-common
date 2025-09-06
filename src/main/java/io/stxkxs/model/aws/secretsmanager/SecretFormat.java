package io.stxkxs.model.aws.secretsmanager;

/**
 * Secret format configuration for password generation.
 */
public record SecretFormat(boolean excludeLowercase, boolean excludeNumbers, boolean excludeUppercase, boolean includeSpace, int length,
  boolean requireEachIncludedType) {}
