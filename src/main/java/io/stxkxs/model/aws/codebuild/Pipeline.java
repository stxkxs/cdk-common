package io.stxkxs.model.aws.codebuild;

import java.util.List;
import software.amazon.awscdk.services.codepipeline.ExecutionMode;
import software.amazon.awscdk.services.codepipeline.PipelineType;

/**
 * Pipeline configuration for AWS CodeBuild.
 */
public record Pipeline(String name, String description, PipelineType pipelineType, ExecutionMode executionMode, List<Variable> variables,
  boolean crossAccountKeys, boolean restartExecutionOnUpdate, CodeStarConnectionSource cdkRepo, CodeStarConnectionSource deployment) {}
