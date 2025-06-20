package io.stxkxs.model.aws.cognito.userpool;

public record PasswordPolicy(
  int minLength,
  boolean requireLowercase,
  boolean requireUppercase,
  boolean requireDigits,
  boolean requireSymbols,
  int tempPasswordValidity
) {}
