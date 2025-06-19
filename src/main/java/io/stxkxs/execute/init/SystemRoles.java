package io.stxkxs.execute.init;

import io.stxkxs.execute.aws.iam.RoleConstruct;
import io.stxkxs.model._main.Common;
import io.stxkxs.model._main.SynthesizerResources;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.NestedStack;
import software.amazon.awscdk.NestedStackProps;
import software.amazon.awscdk.services.iam.Role;
import software.constructs.Construct;

import static io.stxkxs.execute.serialization.Format.describe;
import static io.stxkxs.execute.serialization.Format.exported;
import static io.stxkxs.execute.serialization.Format.id;

@Slf4j
@Getter
public class SystemRoles extends NestedStack {
  private final Role cdkExec;
  private final Role cdkDeploy;
  private final Role cdkLookup;
  private final Role cdkAssets;
  private final Role cdkImages;

  public SystemRoles(Construct scope, Common common, SynthesizerResources conf, NestedStackProps props) {
    super(scope, "synthesizer.roles", props);

    log.debug("cdk synthesizer roles configuration [common: {} resources: {}]", common, conf);

    this.cdkExec = new RoleConstruct(this, common, conf.cdkExec()).role();
    this.cdkDeploy = new RoleConstruct(this, common, conf.cdkDeploy()).role();
    this.cdkLookup = new RoleConstruct(this, common, conf.cdkLookup()).role();
    this.cdkAssets = new RoleConstruct(this, common, conf.cdkAssets()).role();
    this.cdkImages = new RoleConstruct(this, common, conf.cdkImages()).role();

    CfnOutput.Builder
      .create(this, id(common.id(), "cdk.exec.role.arn"))
      .exportName(exported(scope, "cdkexecrolearn"))
      .value(this.cdkExec().getRoleArn())
      .description(describe(common, "exec role arn"))
      .build();

    CfnOutput.Builder
      .create(this, id(common.id(), "cdk.deploy.role.arn"))
      .exportName(exported(scope, "cdkdeployrolearn"))
      .value(this.cdkDeploy().getRoleArn())
      .description(describe(common, "deploy role arn"))
      .build();

    CfnOutput.Builder
      .create(this, id(common.id(), "cdk.lookup.role.arn"))
      .exportName(exported(scope, "cdklookuprolearn"))
      .value(this.cdkLookup().getRoleArn())
      .description(describe(common, "lookup role arn"))
      .build();

    CfnOutput.Builder
      .create(this, id(common.id(), "cdk.assets.role.arn"))
      .exportName(exported(scope, "cdkassetsrolearn"))
      .value(this.cdkAssets().getRoleArn())
      .description(describe(common, "assets role arn"))
      .build();

    CfnOutput.Builder
      .create(this, id(common.id(), "cdk.images.role.arn"))
      .exportName(exported(scope, "cdkimagesrolearn"))
      .value(this.cdkImages().getRoleArn())
      .description(describe(common, "images role arn"))
      .build();
  }
}
