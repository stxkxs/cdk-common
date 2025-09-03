package io.stxkxs.execute.aws.eks.addon;

import com.fasterxml.jackson.core.type.TypeReference;
import io.stxkxs.execute.serialization.Mapper;
import io.stxkxs.execute.serialization.Template;
import io.stxkxs.model._main.Common;
import io.stxkxs.model.aws.eks.addon.core.GrafanaAddon;
import io.stxkxs.model.aws.eks.addon.core.GrafanaSecret;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
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

/**
 * Advanced Grafana Helm chart deployment construct with runtime secret management 
 * and secure credential injection for observability dashboards and monitoring.
 * 
 * <p>This construct represents one of the most complex add-on deployments, featuring:
 * 
 * <p><b>Core Capabilities:</b>
 * <ul>
 *   <li><b>Helm Chart Deployment</b> - Production-ready Grafana installation on EKS</li>
 *   <li><b>Runtime Secret Retrieval</b> - Direct AWS Secrets Manager API integration</li>
 *   <li><b>Template Processing</b> - Dynamic configuration injection with secret values</li>
 *   <li><b>Multi-Datasource Support</b> - Loki, Prometheus, Tempo integration</li>
 * </ul>
 * 
 * <p><b>Secret Management Integration:</b>
 * <ul>
 *   <li><b>AWS Secrets Manager</b> - Secure retrieval of datasource credentials</li>
 *   <li><b>Multiple Lookup Methods</b> - Secret name and ARN-based resolution</li>
 *   <li><b>Runtime Injection</b> - Secrets injected into Helm values during deployment</li>
 *   <li><b>Error Handling</b> - Graceful degradation when secrets are unavailable</li>
 * </ul>
 * 
 * <p><b>Supported Datasources:</b>
 * <ul>
 *   <li><b>Loki</b> - Log aggregation with host and username configuration</li>
 *   <li><b>Prometheus</b> - Metrics collection with authentication credentials</li>
 *   <li><b>Tempo</b> - Distributed tracing with secure endpoint access</li>
 *   <li><b>API Key</b> - Grafana service account authentication</li>
 * </ul>
 * 
 * <p><b>Security Features:</b>
 * <ul>
 *   <li>Credential isolation through AWS Secrets Manager</li>
 *   <li>Default AWS credentials provider integration</li>
 *   <li>Regional secret resolution with fallback ARN lookup</li>
 *   <li>Runtime credential injection without persistent storage</li>
 * </ul>
 * 
 * <p><b>Error Handling Strategy:</b>
 * The construct implements a fail-safe approach where missing or inaccessible secrets
 * result in null chart creation rather than deployment failure, allowing the parent
 * construct to handle the degraded state gracefully.
 * 
 * <p><b>Template Processing Flow:</b>
 * <pre>
 * Secret Retrieval → Credential Extraction → Template Processing → Helm Deployment
 *        ↓                    ↓                      ↓                   ↓
 * AWS API Call    →    JSON Parsing    →    Mustache    →    EKS Cluster
 * </pre>
 * 
 * <p><b>Usage Example:</b>
 * <pre>{@code
 * GrafanaConstruct grafana = new GrafanaConstruct(
 *     this, common, grafanaConfig, cluster);
 *     
 * // Automatically handles:
 * // - Secret retrieval from AWS Secrets Manager
 * // - Template value injection with credentials  
 * // - Helm chart deployment with 15-minute timeout
 * // - Namespace and RBAC setup
 * }</pre>
 * 
 * @author CDK Common Framework
 * @since 1.0.0
 * @see HelmChart for Kubernetes deployment mechanism
 * @see SecretsManagerClient for AWS secret retrieval
 * @see GrafanaSecret for credential data structure
 * @see Template for mustache processing capabilities
 */
@Slf4j
@Getter
public class GrafanaConstruct extends Construct {
  private final HelmChart chart;

  @SneakyThrows
  public GrafanaConstruct(Construct scope, Common common, GrafanaAddon conf, ICluster cluster) {
    super(scope, id("grafana", conf.chart().release()));

    log.debug("{} [common: {} conf: {}]", "GrafanaConstruct", common, conf);

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
