package io.stxkxs.execute.aws.lambda;

import io.stxkxs.execute.aws.iam.RoleConstruct;
import io.stxkxs.model._main.Common;
import io.stxkxs.model.aws.fn.Lambda;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.services.ec2.IVpc;
import software.amazon.awscdk.services.ec2.SubnetSelection;
import software.amazon.awscdk.services.ec2.SubnetType;
import software.amazon.awscdk.services.lambda.Architecture;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.CodeSigningConfig;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Function.Builder;
import software.amazon.awscdk.services.lambda.LayerVersion;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.signer.Platform;
import software.amazon.awscdk.services.signer.SigningProfile;
import software.constructs.Construct;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static io.stxkxs.execute.serialization.Format.id;

@Slf4j
@Getter
public class LambdaConstruct extends Construct {
  private final Function function;

  public LambdaConstruct(Construct scope, Common common, Lambda conf, IVpc vpc) {
    super(scope, id("lambda", conf.name()));

    log.debug("lamaba configuration [common: {} lambda: {}]", common, conf);

    this.function = build(common, conf, vpc, layers(conf));
  }

  public LambdaConstruct(Construct scope, Common common, Lambda conf, IVpc vpc, LayerVersion... layers) {
    super(scope, id("lambda", conf.name()));

    log.debug("lambda with layers configuration [common: {} lambda: {}]", common, conf);

    this.function = build(common, conf, vpc, layers(conf, layers));
  }

  private Function build(Common common, Lambda conf, IVpc vpc, List<LayerVersion> layers) {
    var role = new RoleConstruct(this, common, conf.role()).role();
    return Builder
      .create(this, conf.name())
      .vpc(vpc)
      .vpcSubnets(SubnetSelection.builder()
        .subnetType(SubnetType.valueOf(conf.subnetType().toUpperCase()))
        .build())
      .role(role)
      .functionName(conf.name())
      .description(conf.description())
      .runtime(conf.runtime())
      .architecture(Architecture.X86_64)
      .codeSigningConfig(CodeSigningConfig.Builder.create(this, id("code.signing", conf.name()))
        .signingProfiles(List.of(SigningProfile.Builder.create(this, id("signing.profile", conf.name()))
          .platform(Platform.AWS_LAMBDA_SHA384_ECDSA)
          .build()))
        .build())
      .code(Code.fromAsset(conf.asset()))
      .environment(conf.environment())
      .handler(conf.handler())
      .timeout(Duration.seconds(conf.timeout()))
      .memorySize(conf.memorySize())
      .layers(layers)
      .build();
  }

  private List<LayerVersion> layers(Lambda conf, LayerVersion... layers) {
    return Stream.concat(
        conf.layers().stream()
          .map(layer -> LayerVersion.Builder.create(this, id("layer", layer.name()))
            .layerVersionName(layer.name())
            .code(Code.fromAsset(conf.asset()))
            .removalPolicy(layer.removalPolicy())
            .compatibleArchitectures(List.of(Architecture.X86_64))
            .compatibleRuntimes(layer.runtimes().stream()
              .map(r -> Runtime.Builder.create(r).build()).toList())
            .build()),
        Optional.ofNullable(layers).stream()
          .flatMap(Arrays::stream))
      .toList();
  }
}
