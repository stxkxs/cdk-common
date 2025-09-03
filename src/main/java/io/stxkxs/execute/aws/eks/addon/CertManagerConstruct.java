package io.stxkxs.execute.aws.eks.addon;

import com.fasterxml.jackson.core.type.TypeReference;
import io.stxkxs.execute.serialization.Mapper;
import io.stxkxs.execute.serialization.Template;
import io.stxkxs.model._main.Common;
import io.stxkxs.model.aws.eks.addon.core.CertManagerAddon;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.services.eks.HelmChart;
import software.amazon.awscdk.services.eks.ICluster;
import software.constructs.Construct;

import java.util.Map;

import static io.stxkxs.execute.serialization.Format.id;

@Slf4j
@Getter
public class CertManagerConstruct extends Construct {
  private final HelmChart chart;

  @SneakyThrows
  public CertManagerConstruct(Construct scope, Common common, CertManagerAddon conf, ICluster cluster) {
    super(scope, id("certmanager", conf.chart().release()));

    log.debug("{} [common: {} conf: {}]", "CertManagerConstruct", common, conf);

    var parsed = Template.parse(scope, conf.chart().values());
    var values = Mapper.get().readValue(parsed, new TypeReference<Map<String, Object>>() {});

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
      .values(values)
      .build();
  }
}
