package io.stxkxs.execute.init;

import io.stxkxs.execute.aws.ecr.EcrRepositoryConstruct;
import io.stxkxs.execute.aws.s3.BucketConstruct;
import io.stxkxs.model._main.Common;
import io.stxkxs.model._main.SynthesizerResources;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.NestedStack;
import software.amazon.awscdk.NestedStackProps;
import software.amazon.awscdk.services.ecr.Repository;
import software.amazon.awscdk.services.s3.Bucket;
import software.constructs.Construct;

import static io.stxkxs.execute.serialization.Format.describe;
import static io.stxkxs.execute.serialization.Format.exported;
import static io.stxkxs.execute.serialization.Format.id;

@Slf4j
@Getter
public class SystemStorage extends NestedStack {
  private final Repository cdkEcr;
  private final Bucket cdkAssets;

  public SystemStorage(Construct scope, Common common, SynthesizerResources conf, NestedStackProps props) {
    super(scope, "synthesizer.storage", props);

    log.debug("cdk synthesizer configuration [common: {} resources: {}]", common, conf);

    this.cdkEcr = new EcrRepositoryConstruct(this, common, conf.ecr()).repository();
    this.cdkAssets = new BucketConstruct(this, common, conf.assets()).bucket();

    CfnOutput.Builder
      .create(this, id(common.id(), "cdk.ecr.assets.arn"))
      .exportName(exported(scope, "cdkecrassetsarn"))
      .value(this.cdkEcr().getRepositoryArn())
      .description(describe(common, " repository arn"))
      .build();

    CfnOutput.Builder
      .create(this, id(common.id(), "cdk.bucket.assets.arn"))
      .exportName(exported(scope, "cdkbucketassetsarn"))
      .value(this.cdkAssets().getBucketArn())
      .description(describe(common, "bucket assets arn"))
      .build();
  }
}
