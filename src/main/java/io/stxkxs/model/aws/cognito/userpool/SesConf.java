package io.stxkxs.model.aws.cognito.userpool;

import io.stxkxs.model.aws.ses.Sender;

public record SesConf(
  boolean enabled,
  Sender sender
) {}
