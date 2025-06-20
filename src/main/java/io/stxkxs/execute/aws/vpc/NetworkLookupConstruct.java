package io.stxkxs.execute.aws.vpc;

import io.stxkxs.model._main.Common;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awscdk.services.ec2.IVpc;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ec2.VpcLookupOptions;
import software.constructs.Construct;

import static io.stxkxs.execute.serialization.Format.id;

@Slf4j
@Getter
public class NetworkLookupConstruct extends Construct {
  private final IVpc vpc;

  @SneakyThrows
  public NetworkLookupConstruct(Construct scope, Common common, String name) {
    super(scope, id("network.lookup", name));

    log.debug("network lookup [common: {} name: {}]", common, name);

    if ("true".equals(scope.getNode().tryGetContext("synthesizer"))) {
      log.warn("executing cdk synth ... --context synthesizer ... to validate stack without vpc lookup!");
      this.vpc = Vpc.Builder.create(scope, "synthesizer").build();
      return;
    }

    this.vpc = Vpc.fromLookup(
      scope, "vpc.lookup",
      VpcLookupOptions.builder()
        .ownerAccountId(common.account())
        .region(common.region())
        .vpcName(name)
        .isDefault(false)
        .build());
  }
}
