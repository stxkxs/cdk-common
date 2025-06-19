package io.stxkxs.execute.aws.eks;

import io.stxkxs.execute.aws.iam.RoleConstruct;
import io.stxkxs.model._main.Common;
import io.stxkxs.model._main.Common.Maps;
import io.stxkxs.model.aws.eks.PodIdentity;
import io.stxkxs.model.aws.eks.ServiceAccountConf;
import io.stxkxs.model.aws.iam.Principal;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awscdk.services.eks.ICluster;
import software.amazon.awscdk.services.eks.ServiceAccount;
import software.amazon.awscdk.services.iam.SessionTagsPrincipal;
import software.constructs.Construct;

import java.util.Map;

import static io.stxkxs.execute.serialization.Format.id;

@Slf4j
@Getter
public class ServiceAccountConstruct extends Construct {
  private final String AWS_ROLE_ARN = "eks.amazonaws.com/role-arn";

  private final RoleConstruct roleConstruct;
  private final ServiceAccount serviceAccount;

  @SneakyThrows
  public ServiceAccountConstruct(Construct scope, Common common, ServiceAccountConf conf, ICluster cluster) {
    super(scope, id("service-account", conf.metadata().getName()));

    log.debug("service account configuration [common: {} service-account: {}]", common, conf);

    var oidc = cluster.getOpenIdConnectProvider();
    var principal = Principal.builder().build().oidcPrincipal(scope, oidc, conf);
    this.roleConstruct = new RoleConstruct(this, common, principal, conf.role());
    this.serviceAccount = ServiceAccount.Builder
      .create(this, conf.metadata().getName())
      .cluster(cluster)
      .name(conf.metadata().getName())
      .namespace(conf.metadata().getNamespace())
      .labels(conf.metadata().getLabels())
      .annotations(Maps.from(
        conf.metadata().getAnnotations(),
        Map.of(AWS_ROLE_ARN, this.roleConstruct().role().getRoleArn())))
      .build();
  }

  @SneakyThrows
  public ServiceAccountConstruct(Construct scope, Common common, PodIdentity conf, ICluster cluster) {
    super(scope, id("service-account", conf.metadata().getName()));

    log.debug("pod identity configuration [common: {} pod-identity: {}]", common, conf);

    var principal = new SessionTagsPrincipal(conf.role().principal().iamPrincipal());
    this.roleConstruct = new RoleConstruct(this, common, principal, conf.role());
    this.serviceAccount = ServiceAccount.Builder
      .create(this, conf.metadata().getName())
      .cluster(cluster)
      .name(conf.metadata().getName())
      .namespace(conf.metadata().getNamespace())
      .labels(conf.metadata().getLabels())
      .annotations(Maps.from(
        conf.metadata().getAnnotations(),
        Map.of(AWS_ROLE_ARN, this.roleConstruct().role().getRoleArn())))
      .build();
  }
}
