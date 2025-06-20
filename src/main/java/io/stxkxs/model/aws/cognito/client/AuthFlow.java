package io.stxkxs.model.aws.cognito.client;

public record AuthFlow(
  boolean adminUserPassword,
  boolean custom,
  boolean userPassword,
  boolean userSrp
) {}
