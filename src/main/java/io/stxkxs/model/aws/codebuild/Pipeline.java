package io.stxkxs.model.aws.codebuild;

import software.amazon.awscdk.services.codepipeline.ExecutionMode;
import software.amazon.awscdk.services.codepipeline.PipelineType;

import java.util.List;

public record Pipeline(
  String name,
  String description,
  PipelineType pipelineType,
  ExecutionMode executionMode,
  List<Variable> variables,
  boolean crossAccountKeys,
  boolean restartExecutionOnUpdate,
  CodeStarConnectionSource cdkRepo,
  CodeStarConnectionSource deployment
) {}
