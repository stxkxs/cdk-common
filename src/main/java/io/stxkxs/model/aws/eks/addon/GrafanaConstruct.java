package io.stxkxs.model.aws.eks.addon;

import com.fasterxml.jackson.core.type.TypeReference;
import io.stxkxs.execute.serialization.Mapper;
import io.stxkxs.execute.serialization.Template;
import io.stxkxs.model._main.Common;
import io.stxkxs.model.aws.eks.addon.core.GrafanaAddon;
import io.stxkxs.model.aws.eks.addon.core.GrafanaSecret;
import lombok.Getter;
import lombok.SneakyThrows;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.services.eks.HelmChart;
import software.amazon.awscdk.services.eks.ICluster;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.constructs.Construct;

import java.util.Map;

import static io.stxkxs.execute.serialization.Format.id;

@Getter
public class GrafanaConstruct extends Construct {
  private final HelmChart chart;

  @SneakyThrows
  public GrafanaConstruct(Construct scope, Common common, GrafanaAddon conf, ICluster cluster) {
    super(scope, id("grafana", conf.chart().release()));

    var secret = secret(common, conf.secret());
    if (secret == null) {
      this.chart = null;
      return;
    }

    var parsed = Template.parse(scope, conf.chart().values(),
      Map.ofEntries(
        Map.entry("key", secret.key()),
        Map.entry("lokiHost", secret.lokiHost()),
        Map.entry("lokiUsername", secret.lokiUsername()),
        Map.entry("prometheusHost", secret.prometheusHost()),
        Map.entry("prometheusUsername", secret.prometheusUsername()),
        Map.entry("tempoHost", secret.tempoHost()),
        Map.entry("tempoUsername", secret.tempoUsername())));

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

  private static GrafanaSecret secret(Common common, String secret) {
    if (secret == null || secret.isEmpty())
      return null;

    try (var client = SecretsManagerClient.builder()
      .region(Region.of(common.region()))
      .credentialsProvider(DefaultCredentialsProvider.create())
      .build()) {

      var secretValueResponse = get(client, secret);
      if (secretValueResponse != null)
        return secretValueResponse;

      var arn = String.format("arn:aws:secretsmanager:%s:%s:secret:%s", common.region(), common.account(), secret);
      return get(client, arn);
    } catch (Exception e) {
      throw new RuntimeException("failed to retrieve grafana secret " + e.getMessage(), e);
    }
  }

  private static GrafanaSecret get(SecretsManagerClient client, String id) {
    try {
      var secret = client.getSecretValue(
        GetSecretValueRequest.builder()
          .secretId(id)
          .build());

      var value = secret.secretString();
      if (value != null)
        return Mapper.get().readValue(value, GrafanaSecret.class);

      return null;
    } catch (Exception e) {
      return null;
    }
  }
}
