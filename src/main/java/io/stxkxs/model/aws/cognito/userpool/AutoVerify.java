package io.stxkxs.model.aws.cognito.userpool;

public record AutoVerify(
  boolean email,
  boolean phone
) {}
