package io.stxkxs.model.aws.codebuild;

import static java.util.Map.Entry;
import static java.util.Map.entry;
import static java.util.stream.Collectors.toMap;

import java.util.Map;
import lombok.Builder;
import software.amazon.awscdk.services.codebuild.BuildEnvironmentVariable;
import software.amazon.awscdk.services.codebuild.BuildEnvironmentVariableType;
import software.amazon.awscdk.services.codebuild.ComputeType;

/**
 * Build environment configuration for AWS CodeBuild.
 */
@Builder
public record Environment(ComputeType computeType, Map<String, String> variables, boolean privileged, Certificate certificate) {

  /**
   * Converts plain string variables to BuildEnvironmentVariable objects.
   *
   * @return map of environment variables for CodeBuild
   */
  public Map<String, BuildEnvironmentVariable> environmentVariables() {
    return this.variables().entrySet().stream()
      .map(kv -> entry(kv.getKey(),
        BuildEnvironmentVariable.builder().type(BuildEnvironmentVariableType.PLAINTEXT).value(kv.getValue()).build()))
      .collect(toMap(Entry::getKey, Entry::getValue));
  }
}
