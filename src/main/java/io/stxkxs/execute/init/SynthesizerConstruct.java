package io.stxkxs.execute.init;

import io.stxkxs.execute.aws.kms.KmsConstruct;
import io.stxkxs.model._main.Common;
import io.stxkxs.model._main.SynthesizerResources;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awscdk.NestedStack;
import software.amazon.awscdk.NestedStackProps;
import software.amazon.awscdk.Tags;
import software.amazon.awscdk.services.ssm.ParameterDataType;
import software.amazon.awscdk.services.ssm.StringParameter;
import software.constructs.Construct;

import static io.stxkxs.execute.serialization.Format.describe;

@Slf4j
@Getter
public class SynthesizerConstruct extends NestedStack {
  private final KmsConstruct key;
  private final StringParameter version;
  private final SystemRolesConstruct roles;
  private final SystemStorageConstruct storage;

  public SynthesizerConstruct(Construct scope, Common common, SynthesizerResources conf, NestedStackProps props) {
    super(scope, "synthesizer.owner", props);

    log.debug("{} [common: {} conf: {} props: {}]", "SynthesizerConstruct", common, conf, props);

    this.key = new KmsConstruct(this, common, conf.kms());

    var parent = scope.getNode().getContext("host:id").toString();
    this.version = StringParameter.Builder
      .create(this, "ssm")
      .parameterName(String.format("/cdk/%s-%s/version", parent, common.id()))
      .stringValue("21")
      .description("cdk version")
      .dataType(ParameterDataType.TEXT)
      .build();

    common.tags().forEach((k, v) -> Tags.of(this.version()).add(k, v));

    this.roles = new SystemRolesConstruct(
      this, common, conf,
      NestedStackProps.builder()
        .description(describe(common, "roles & policies"))
        .build());

    this.storage = new SystemStorageConstruct(
      this, common, conf,
      NestedStackProps.builder()
        .description(describe(common, "ecr & s3 storage"))
        .build());
  }
}
