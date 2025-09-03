package io.stxkxs.execute.aws.codebuild;

import com.fasterxml.jackson.core.type.TypeReference;
import io.stxkxs.execute.aws.cloudwatch.LogGroupConstruct;
import io.stxkxs.execute.serialization.Mapper;
import io.stxkxs.execute.serialization.Template;
import io.stxkxs.model._main.Common;
import io.stxkxs.model.aws.codebuild.BuildProject;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awscdk.services.codebuild.Artifacts;
import software.amazon.awscdk.services.codebuild.BuildSpec;
import software.amazon.awscdk.services.codebuild.Cache;
import software.amazon.awscdk.services.codebuild.CloudWatchLoggingOptions;
import software.amazon.awscdk.services.codebuild.IBuildImage;
import software.amazon.awscdk.services.codebuild.LoggingOptions;
import software.amazon.awscdk.services.codebuild.Project;
import software.amazon.awscdk.services.codebuild.Project.Builder;
import software.amazon.awscdk.services.codebuild.S3ArtifactsProps;
import software.amazon.awscdk.services.iam.IRole;
import software.amazon.awscdk.services.logs.ILogGroup;
import software.amazon.awscdk.services.s3.IBucket;
import software.constructs.Construct;

import java.util.Map;

import static io.stxkxs.execute.serialization.Format.id;

@Slf4j
@Getter
public class ProjectConstruct extends Construct {
  private final Project project;
  private final ILogGroup logGroup;
  private final LoggingOptions loggingOptions;
  private final Cache cache;
  private final IRole role;
  private final IBucket assets;

  @SneakyThrows
  public ProjectConstruct(Construct scope, Common common, BuildProject conf, IBucket assets, IRole role, IBuildImage buildImage) {
    super(scope, id("codebuild-project", conf.name()));

    var environment = DecideBuildEnvironment.from(this, common, conf.environment(), buildImage);

    var yaml = Template.parse(this, conf.buildspec());
    var buildspec = Mapper.get().readValue(yaml, new TypeReference<Map<String, Object>>() {});

    log.debug("{} [common: {} conf: {}]", "ProjectConstruct", common, conf);

    this.assets = assets;
    this.role = role;

    this.logGroup = new LogGroupConstruct(scope, common, conf.logging().logGroup()).logGroup();
    this.loggingOptions = LoggingOptions.builder()
      .cloudWatch(CloudWatchLoggingOptions.builder()
        .logGroup(this.logGroup())
        .prefix(conf.logging().prefix())
        .enabled(conf.logging().enabled())
        .build())
      .build();

    if (conf.cache())
      this.cache = Cache.bucket(assets);
    else this.cache = Cache.none();

    this.project = Builder
      .create(scope, id("project", conf.name()))
      .role(this.role())
      .artifacts(Artifacts.s3(
        S3ArtifactsProps.builder()
          .bucket(assets)
          .includeBuildId(true)
          .build()))
      .cache(this.cache())
      .logging(this.loggingOptions())
      .grantReportGroupPermissions(false)
      .projectName(conf.name())
      .description(conf.description())
      .buildSpec(BuildSpec.fromObjectToYaml(buildspec))
      .concurrentBuildLimit(conf.concurrentBuildLimit())
      .environment(environment)
      .environmentVariables(conf.environment().environmentVariables())
      .badge(conf.badge())
      .build();
  }
}
