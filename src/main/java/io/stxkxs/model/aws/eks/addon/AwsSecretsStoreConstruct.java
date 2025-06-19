package io.stxkxs.model.aws.eks.addon;

import io.stxkxs.model._main.Common;
import io.stxkxs.model.aws.eks.addon.core.secretprovider.AwsSecretsStoreAddon;
import lombok.Getter;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.services.eks.HelmChart;
import software.amazon.awscdk.services.eks.ICluster;
import software.constructs.Construct;

import static io.stxkxs.execute.serialization.Format.id;

@Getter
public class AwsSecretsStoreConstruct extends Construct {
  private final HelmChart chart;

  public AwsSecretsStoreConstruct(Construct scope, Common common, AwsSecretsStoreAddon conf, ICluster cluster) {
    super(scope, id("awssecretsstore", conf.chart().release()));

    this.chart = HelmChart.Builder
      .create(this, conf.chart().name())
      .cluster(cluster)
      .wait(true)
      .timeout(Duration.minutes(15))
      .skipCrds(false)
      .createNamespace(true)
      .chart(conf.chart().name())
      .namespace(conf.chart().namespace())
      .repository(conf.chart().repository())
      .release(conf.chart().release())
      .version(conf.chart().version())
      .build();
  }
}
