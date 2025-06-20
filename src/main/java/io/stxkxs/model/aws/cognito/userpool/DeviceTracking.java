package io.stxkxs.model.aws.cognito.userpool;

public record DeviceTracking(
  boolean newDeviceChallenge,
  boolean rememberOnUserPrompt
) {}
