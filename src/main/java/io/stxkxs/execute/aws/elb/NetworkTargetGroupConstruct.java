package io.stxkxs.execute.aws.elb;

import static io.stxkxs.execute.serialization.Format.id;

import io.stxkxs.model._main.Common;
import io.stxkxs.model._main.Common.Maps;
import io.stxkxs.model.aws.loadbalancer.LoadBalancer;
import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awscdk.Tags;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.elasticloadbalancingv2.BaseNetworkListenerProps;
import software.amazon.awscdk.services.elasticloadbalancingv2.HealthCheck;
import software.amazon.awscdk.services.elasticloadbalancingv2.NetworkListener;
import software.amazon.awscdk.services.elasticloadbalancingv2.NetworkListenerAction;
import software.amazon.awscdk.services.elasticloadbalancingv2.NetworkLoadBalancer;
import software.amazon.awscdk.services.elasticloadbalancingv2.NetworkTargetGroup;
import software.amazon.awscdk.services.elasticloadbalancingv2.Protocol;
import software.amazon.awscdk.services.elasticloadbalancingv2.TargetType;
import software.constructs.Construct;

@Slf4j
@Getter
public class NetworkTargetGroupConstruct extends Construct {
  private final NetworkTargetGroup networkTargetGroup;
  private final NetworkListener networkListener;

  public NetworkTargetGroupConstruct(Construct scope, Common common, LoadBalancer conf, Vpc vpc, NetworkLoadBalancer networkLoadBalancer) {
    super(scope, id("network.target-group", conf.defaultTargetGroup().name()));

    log.debug("{} [common: {} conf: {}]", "NetworkTargetGroupConstruct", common, conf);

    var target = conf.defaultTargetGroup();
    this.networkTargetGroup = NetworkTargetGroup.Builder.create(this, target.name()).vpc(vpc).port(target.port())
      .protocol(Protocol.valueOf(target.protocol().toUpperCase())).targetType(TargetType.valueOf(target.type().toUpperCase()))
      .targetGroupName(target.name()).preserveClientIp(target.preserveClientIp()).connectionTermination(target.connectionTermination())
      .healthCheck(HealthCheck.builder().enabled(target.healthcheck().enabled())
        .protocol(Protocol.valueOf(target.healthcheck().protocol().toUpperCase())).healthyHttpCodes(target.healthcheck().healthyHttpCodes())
        .port(target.healthcheck().port()).path(target.healthcheck().path()).build())
      .build();

    this.networkListener = networkLoadBalancer.addListener(id(target.name(), target.port() + ""),
      BaseNetworkListenerProps.builder().port(target.port()).protocol(Protocol.valueOf(target.protocol().toUpperCase()))
        .defaultAction(NetworkListenerAction.forward(List.of(this.networkTargetGroup()))).build());

    Maps.from(conf.tags(), common.tags()).forEach((key, value) -> {
      Tags.of(this.networkTargetGroup()).add(key, value);
      Tags.of(this.networkListener()).add(key, value);
    });
  }
}
