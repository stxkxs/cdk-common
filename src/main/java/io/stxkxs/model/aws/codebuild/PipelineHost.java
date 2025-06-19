package io.stxkxs.model.aws.codebuild;

import io.stxkxs.model._main.Common;

public record PipelineHost<T>(
  Common common,
  CodeStarConnectionSource source,
  Pipeline pipeline,
  String synthesizer
) {}
