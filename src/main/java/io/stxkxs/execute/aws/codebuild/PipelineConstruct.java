package io.stxkxs.execute.aws.codebuild;

import io.stxkxs.model._main.Common;
import io.stxkxs.model.aws.codebuild.Pipeline;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awscdk.services.codepipeline.Pipeline.Builder;
import software.amazon.awscdk.services.codepipeline.Variable;
import software.amazon.awscdk.services.iam.IRole;
import software.amazon.awscdk.services.s3.IBucket;
import software.constructs.Construct;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import static io.stxkxs.execute.serialization.Format.id;
import static java.util.stream.Collectors.toMap;

@Slf4j
@Getter
public class PipelineConstruct extends Construct {
  private final Builder get;

  @SneakyThrows
  public PipelineConstruct(Construct scope, Common common, Pipeline conf, IBucket assets, IRole role) {
    super(scope, "pipeline");

    log.debug("codepipeline configuration [common: {} codepipeline: {}]", common, conf);

    var variables = conf
      .variables()
      .stream()
      .sorted()
      .map(v -> Map.entry(v.name(),
        Variable.Builder.create()
          .variableName(v.name())
          .defaultValue(v.defaults())
          .build()))
      .collect(toMap(Entry::getKey, Entry::getValue, (existing, replacement) -> existing, LinkedHashMap::new));

    this.get = Builder
      .create(scope, id(common.id(), "pipeline"))
      .variables(variables.values().stream().toList())
      .pipelineName(conf.name())
      .pipelineType(conf.pipelineType())
      .executionMode(conf.executionMode())
      .crossAccountKeys(conf.crossAccountKeys())
      .restartExecutionOnUpdate(conf.restartExecutionOnUpdate())
      .artifactBucket(assets)
      .role(role);
  }
}
