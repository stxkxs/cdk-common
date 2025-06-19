package io.stxkxs.execute.aws.codebuild;

import software.amazon.awscdk.services.codebuild.BuildEnvironmentVariable;
import software.amazon.awscdk.services.codebuild.BuildEnvironmentVariableType;

public interface PipelineCreator {
  default BuildEnvironmentVariable toBuildEnvironmentVariable(String s) {
    return BuildEnvironmentVariable.builder()
      .type(BuildEnvironmentVariableType.PLAINTEXT)
      .value(s)
      .build();
  }
}
