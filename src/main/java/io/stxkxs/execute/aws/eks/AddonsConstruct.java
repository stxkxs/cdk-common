package io.stxkxs.execute.aws.eks;

import static io.stxkxs.execute.serialization.Format.id;

import io.stxkxs.execute.aws.eks.addon.AlloyOperatorConstruct;
import io.stxkxs.execute.aws.eks.addon.AwsLoadBalancerConstruct;
import io.stxkxs.execute.aws.eks.addon.AwsSecretsStoreConstruct;
import io.stxkxs.execute.aws.eks.addon.CertManagerConstruct;
import io.stxkxs.execute.aws.eks.addon.CsiSecretsStoreConstruct;
import io.stxkxs.execute.aws.eks.addon.GrafanaConstruct;
import io.stxkxs.execute.aws.eks.addon.KarpenterConstruct;
import io.stxkxs.execute.serialization.Mapper;
import io.stxkxs.execute.serialization.Template;
import io.stxkxs.model._main.Common;
import io.stxkxs.model.aws.eks.KubernetesConf;
import io.stxkxs.model.aws.eks.addon.AddonsConf;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awscdk.services.eks.Cluster;
import software.constructs.Construct;

@Slf4j
@Getter
public class AddonsConstruct extends Construct {
  private final AlloyOperatorConstruct alloyOperator;
  private final GrafanaConstruct grafana;
  private final CertManagerConstruct certManager;
  private final CsiSecretsStoreConstruct csiSecretsStore;
  private final AwsSecretsStoreConstruct awsSecretsStore;
  private final KarpenterConstruct karpenter;
  private final AwsLoadBalancerConstruct awsLoadBalancer;

  @SneakyThrows
  public AddonsConstruct(Construct scope, Common common, KubernetesConf conf, Cluster cluster) {
    super(scope, id("eks.addons", conf.name()));

    log.debug("{} [common: {} conf: {}]", "AddonsConstruct", common, conf);

    var addons = Mapper.get().readValue(Template.parse(scope, conf.addons()), AddonsConf.class);

    this.alloyOperator = new AlloyOperatorConstruct(this, common, addons.alloyOperator(), cluster);

    this.grafana = new GrafanaConstruct(this, common, addons.grafana(), cluster);
    this.grafana().getNode().addDependency(this.alloyOperator());

    this.certManager = new CertManagerConstruct(this, common, addons.certManager(), cluster);
    this.certManager().getNode().addDependency(this.grafana());

    this.csiSecretsStore = new CsiSecretsStoreConstruct(this, common, addons.csiSecretsStore(), cluster);
    this.csiSecretsStore().getNode().addDependency(this.grafana(), this.certManager());

    this.awsSecretsStore = new AwsSecretsStoreConstruct(this, common, addons.awsSecretsStore(), cluster);
    this.awsSecretsStore().getNode().addDependency(this.grafana(), this.certManager(), this.csiSecretsStore());

    this.karpenter = new KarpenterConstruct(this, common, addons.karpenter(), cluster);
    this.karpenter().getNode().addDependency(this.grafana(), this.certManager(), this.csiSecretsStore(), this.awsSecretsStore());

    this.awsLoadBalancer = new AwsLoadBalancerConstruct(this, common, addons.awsLoadBalancer(), cluster);
    this.awsLoadBalancer().getNode().addDependency(this.grafana(), this.certManager(), this.csiSecretsStore(), this.awsSecretsStore(),
      this.karpenter());
  }
}
