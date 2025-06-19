package io.stxkxs.model.aws.codebuild;

public record PipelineHosted<T, U>(
  PipelineHost<T> host,
  U hosted
) {}
