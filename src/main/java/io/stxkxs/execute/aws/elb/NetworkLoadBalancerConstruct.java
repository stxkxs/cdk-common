package io.stxkxs.execute.aws.elb;

import io.stxkxs.model._main.Common;
import io.stxkxs.model._main.Common.Maps;
import io.stxkxs.model.aws.loadbalancer.LoadBalancer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awscdk.Tags;
import software.amazon.awscdk.services.ec2.SecurityGroup;
import software.amazon.awscdk.services.ec2.SubnetSelection;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.elasticloadbalancingv2.NetworkLoadBalancer;
import software.constructs.Construct;

import java.util.List;

import static io.stxkxs.execute.serialization.Format.id;

@Slf4j
@Getter
public class NetworkLoadBalancerConstruct extends Construct {
  private final NetworkLoadBalancer networkLoadBalancer;

  public NetworkLoadBalancerConstruct(Construct scope, Common common, LoadBalancer conf, Vpc vpc, List<SecurityGroup> securityGroups) {
    super(scope, id("network.loadbalancer", conf.name()));

    log.debug("network loadbalancer configuration [common: {} load-balancer: {}]", common, conf);

    this.networkLoadBalancer = NetworkLoadBalancer.Builder
      .create(this, conf.name())
      .loadBalancerName(conf.name())
      .vpc(vpc)
      .vpcSubnets(
        SubnetSelection.builder()
          .availabilityZones(vpc.getAvailabilityZones())
          .subnets(vpc.getPublicSubnets())
          .build())
      .securityGroups(securityGroups)
      .crossZoneEnabled(conf.crossZoneEnabled())
      .deletionProtection(conf.deletionProtection())
      .internetFacing(conf.internetFacing())
      .build();

    Maps.from(common.tags(), conf.tags())
      .forEach((key, value) -> Tags.of(this.networkLoadBalancer()).add(key, value));
  }
}
