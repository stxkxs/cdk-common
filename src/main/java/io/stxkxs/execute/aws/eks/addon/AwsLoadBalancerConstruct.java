package io.stxkxs.execute.aws.eks.addon;

import static io.stxkxs.execute.serialization.Format.id;

import com.fasterxml.jackson.core.type.TypeReference;
import io.stxkxs.execute.aws.eks.NamespaceConstruct;
import io.stxkxs.execute.aws.eks.ServiceAccountConstruct;
import io.stxkxs.execute.serialization.Mapper;
import io.stxkxs.execute.serialization.Template;
import io.stxkxs.model._main.Common;
import io.stxkxs.model.aws.eks.addon.core.AwsLoadBalancerAddon;
import java.util.Map;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.services.eks.HelmChart;
import software.amazon.awscdk.services.eks.ICluster;
import software.constructs.Construct;

@Slf4j
@Getter
public class AwsLoadBalancerConstruct extends Construct {
  private final NamespaceConstruct namespace;
  private final ServiceAccountConstruct serviceAccount;
  private final HelmChart chart;

  @SneakyThrows
  public AwsLoadBalancerConstruct(Construct scope, Common common, AwsLoadBalancerAddon conf, ICluster cluster) {
    super(scope, id("awsloadbalancer", conf.chart().release()));

    log.debug("{} [common: {} conf: {}]", "AwsLoadBalancerConstruct", common, conf);

    this.namespace = new NamespaceConstruct(this, common, conf.serviceAccount().metadata(), cluster);

    this.serviceAccount = new ServiceAccountConstruct(this, common, conf.serviceAccount(), cluster);
    this.serviceAccount().getNode().addDependency(this.namespace());

    var values = Mapper.get().readValue(Template.parse(scope, conf.chart().values()), new TypeReference<Map<String, Object>>() {});
    this.chart = HelmChart.Builder.create(this, conf.chart().name()).cluster(cluster).wait(true).timeout(Duration.minutes(15))
      .skipCrds(false).createNamespace(true).chart(conf.chart().name()).namespace(conf.chart().namespace())
      .repository(conf.chart().repository()).release(conf.chart().release()).version(conf.chart().version()).values(values).build();

    this.chart().getNode().addDependency(this.serviceAccount());
  }
}
