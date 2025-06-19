package io.stxkxs.model.aws.codebuild;

import lombok.Builder;
import software.amazon.awscdk.services.codebuild.BuildEnvironmentVariable;
import software.amazon.awscdk.services.codebuild.BuildEnvironmentVariableType;
import software.amazon.awscdk.services.codebuild.ComputeType;

import java.util.Map;

import static java.util.Map.Entry;
import static java.util.Map.entry;
import static java.util.stream.Collectors.toMap;

@Builder
public record Environment(
  ComputeType computeType,
  Map<String, String> variables,
  boolean privileged,
  Certificate certificate
) {

  public Map<String, BuildEnvironmentVariable> environmentVariables() {
    return this.variables()
      .entrySet()
      .stream()
      .map(kv -> entry(kv.getKey(), BuildEnvironmentVariable.builder()
        .type(BuildEnvironmentVariableType.PLAINTEXT)
        .value(kv.getValue())
        .build()))
      .collect(toMap(Entry::getKey, Entry::getValue));
  }
}
