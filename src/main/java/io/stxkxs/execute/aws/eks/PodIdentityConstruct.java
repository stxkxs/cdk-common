package io.stxkxs.execute.aws.eks;

import io.stxkxs.model._main.Common;
import io.stxkxs.model.aws.eks.PodIdentity;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awscdk.services.eks.CfnPodIdentityAssociation;
import software.amazon.awscdk.services.eks.ICluster;
import software.constructs.Construct;

import static io.stxkxs.execute.serialization.Format.id;

@Slf4j
@Getter
public class PodIdentityConstruct extends Construct {
  private final ServiceAccountConstruct serviceAccountConstruct;
  private final CfnPodIdentityAssociation association;

  @SneakyThrows
  public PodIdentityConstruct(Construct scope, Common common, PodIdentity conf, ICluster cluster) {
    super(scope, id("pod-identity-association", conf.metadata().getName()));

    log.debug("pod identity configuration [common: {} pod-identity: {}]", common, conf);

    this.serviceAccountConstruct = new ServiceAccountConstruct(this, common, conf, cluster);
    this.association = CfnPodIdentityAssociation.Builder
      .create(this, conf.metadata().getName())
      .clusterName(cluster.getClusterName())
      .serviceAccount(conf.metadata().getName())
      .namespace(conf.metadata().getNamespace())
      .roleArn(this.serviceAccountConstruct().roleConstruct().role().getRoleArn())
      .build();
  }
}
