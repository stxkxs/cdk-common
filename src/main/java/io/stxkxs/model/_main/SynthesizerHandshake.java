package io.stxkxs.model._main;

import io.stxkxs.model.aws.iam.IamRole;

public record SynthesizerHandshake<T>(
  Common common,
  IamRole handshake,
  SynthesizerResources synthesizer
) {}
