package io.stxkxs.model.aws.eks.addon;

import com.fasterxml.jackson.core.type.TypeReference;
import io.stxkxs.execute.aws.eks.NamespaceConstruct;
import io.stxkxs.execute.aws.eks.PodIdentityConstruct;
import io.stxkxs.execute.serialization.Mapper;
import io.stxkxs.execute.serialization.Template;
import io.stxkxs.model._main.Common;
import io.stxkxs.model.aws.eks.addon.core.karpenter.KarpenterAddon;
import lombok.Getter;
import lombok.SneakyThrows;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.services.eks.HelmChart;
import software.amazon.awscdk.services.eks.ICluster;
import software.amazon.awscdk.services.eks.KubernetesManifest;
import software.constructs.Construct;

import java.util.Map;

import static io.stxkxs.execute.serialization.Format.id;

@Getter
public class KarpenterConstruct extends Construct {
  private final KubernetesManifest namespace;
  private final HelmChart chart;
  private final PodIdentityConstruct podIdentity;

  @SneakyThrows
  public KarpenterConstruct(Construct scope, Common common, KarpenterAddon conf, ICluster cluster) {
    super(scope, id("karpenter"));

    this.namespace = new NamespaceConstruct(this, common, conf.podIdentity().metadata(), cluster).manifest();
    this.podIdentity = new PodIdentityConstruct(this, common, conf.podIdentity(), cluster);
    this.podIdentity().getNode().addDependency(this.namespace());

    var values = Mapper.get().readValue(Template.parse(scope, conf.chart().values()), new TypeReference<Map<String, Object>>() {});
    this.chart = HelmChart.Builder
      .create(this, conf.chart().name())
      .cluster(cluster)
      .wait(true)
      .timeout(Duration.minutes(15))
      .skipCrds(false)
      .createNamespace(false)
      .chart(conf.chart().name())
      .namespace(conf.chart().namespace())
      .repository(conf.chart().repository())
      .release(conf.chart().release())
      .version(conf.chart().version())
      .values(values)
      .build();

    this.chart().getNode().addDependency(this.podIdentity());
  }
}
