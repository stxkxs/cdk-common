package io.stxkxs.execute.aws.codebuild;

import io.stxkxs.execute.aws.s3.BucketConstruct;
import io.stxkxs.model._main.Common;
import io.stxkxs.model.aws.codebuild.Environment;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awscdk.services.codebuild.BuildEnvironment;
import software.amazon.awscdk.services.codebuild.BuildEnvironmentCertificate;
import software.amazon.awscdk.services.codebuild.IBuildImage;
import software.constructs.Construct;

@Slf4j
public class DecideBuildEnvironment {

  public static BuildEnvironment from(Construct scope, Common common, Environment e, IBuildImage buildImage) {
    log.debug("build environment configuration [common: {} environment: {}]", common, e);

    var environment = BuildEnvironment.builder().environmentVariables(e.environmentVariables()).computeType(e.computeType())
      .buildImage(buildImage).privileged(e.privileged());

    Optional.ofNullable(e.certificate().bucket()).ifPresent(b -> environment.certificate(BuildEnvironmentCertificate.builder()
      .bucket(new BucketConstruct(scope, common, b).bucket()).objectKey(e.certificate().objectKey()).build()));

    return environment.build();
  }
}
