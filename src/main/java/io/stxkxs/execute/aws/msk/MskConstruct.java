package io.stxkxs.execute.aws.msk;

import static io.stxkxs.execute.serialization.Format.id;
import static software.amazon.awscdk.services.msk.CfnServerlessCluster.ClientAuthenticationProperty;
import static software.amazon.awscdk.services.msk.CfnServerlessCluster.IamProperty;
import static software.amazon.awscdk.services.msk.CfnServerlessCluster.SaslProperty;
import static software.amazon.awscdk.services.msk.CfnServerlessCluster.VpcConfigProperty;

import io.stxkxs.model._main.Common;
import io.stxkxs.model._main.Common.Maps;
import io.stxkxs.model.aws.msk.Msk;
import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awscdk.services.ec2.ISubnet;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.msk.CfnServerlessCluster;
import software.constructs.Construct;

@Slf4j
@Getter
public class MskConstruct extends Construct {
  private final CfnServerlessCluster msk;

  public MskConstruct(Construct scope, Common common, Msk conf, Vpc vpc, List<String> securityGroupIds) {
    super(scope, id("msk", conf.name()));

    log.debug("{} [common: {} conf: {}]", "MskConstruct", common, conf);

    this.msk = CfnServerlessCluster.Builder.create(this, conf.name()).clusterName(conf.name())
      .vpcConfigs(List.of(VpcConfigProperty.builder().subnetIds(vpc.getPrivateSubnets().stream().map(ISubnet::getSubnetId).toList())
        .securityGroups(securityGroupIds).build()))
      .clientAuthentication(ClientAuthenticationProperty.builder()
        .sasl(SaslProperty.builder().iam(IamProperty.builder().enabled(true).build()).build()).build())
      .tags(Maps.from(common.tags(), conf.tags())).build();
  }
}
