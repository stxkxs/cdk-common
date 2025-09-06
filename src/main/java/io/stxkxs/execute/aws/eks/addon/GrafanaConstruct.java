package io.stxkxs.execute.aws.eks.addon;

import static io.stxkxs.execute.serialization.Format.id;

import com.fasterxml.jackson.core.type.TypeReference;
import io.stxkxs.execute.serialization.Mapper;
import io.stxkxs.execute.serialization.Template;
import io.stxkxs.model._main.Common;
import io.stxkxs.model.aws.eks.addon.core.GrafanaAddon;
import io.stxkxs.model.aws.eks.addon.core.GrafanaSecret;
import java.util.Map;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.services.eks.HelmChart;
import software.amazon.awscdk.services.eks.ICluster;
import software.constructs.Construct;

/**
 * Advanced Grafana Helm chart deployment construct with CDK context-based configuration and secure credential injection for observability
 * dashboards and monitoring.
 *
 * <p>
 * This construct represents one of the most complex add-on deployments, featuring:
 *
 * <p>
 * <b>Core Capabilities:</b>
 * <ul>
 * <li><b>Helm Chart Deployment</b> - Production-ready Grafana installation on EKS</li>
 * <li><b>CDK Context Integration</b> - Direct configuration from cdk.context.json</li>
 * <li><b>Template Processing</b> - Dynamic configuration injection with context values</li>
 * <li><b>Multi-Datasource Support</b> - Loki, Prometheus, Tempo, Pyroscope integration</li>
 * </ul>
 *
 * <p>
 * <b>Configuration Management:</b>
 * <ul>
 * <li><b>CDK Context</b> - Secure configuration from cdk.context.json</li>
 * <li><b>Build-time Injection</b> - Values injected into Helm chart during CDK synthesis</li>
 * <li><b>Error Handling</b> - Graceful degradation when context values are missing</li>
 * </ul>
 *
 * <p>
 * <b>Supported Datasources:</b>
 * <ul>
 * <li><b>Loki</b> - Log aggregation with host and username configuration</li>
 * <li><b>Prometheus</b> - Metrics collection with authentication credentials</li>
 * <li><b>Tempo</b> - Distributed tracing with secure endpoint access</li>
 * <li><b>Pyroscope</b> - Continuous profiling with host configuration</li>
 * <li><b>Fleet Management</b> - Grafana fleet management integration</li>
 * <li><b>API Key</b> - Grafana service account authentication</li>
 * </ul>
 *
 * <p>
 * <b>Security Features:</b>
 * <ul>
 * <li>Credential configuration through CDK context</li>
 * <li>Build-time credential injection without runtime AWS API calls</li>
 * <li>No persistent storage of credentials in the cluster</li>
 * </ul>
 *
 * <p>
 * <b>Template Processing Flow:</b>
 *
 * <pre>
 * Context Reading → Configuration Creation → Template Processing → Helm Deployment
 *        ↓                    ↓                      ↓                   ↓
 * CDK Context    →   GrafanaSecret   →    Mustache    →    EKS Cluster
 * </pre>
 *
 * <p>
 * <b>Usage Example:</b>
 *
 * <pre>{@code
 * GrafanaConstruct grafana = new GrafanaConstruct(this, common, grafanaConfig, cluster);
 *
 * // Automatically handles:
 * // - Configuration retrieval from CDK context
 * // - Template value injection with credentials
 * // - Helm chart deployment with 15-minute timeout
 * // - Namespace and RBAC setup
 * }</pre>
 *
 * @author CDK Common Framework
 * @see HelmChart for Kubernetes deployment mechanism
 * @see GrafanaSecret for credential data structure
 * @see Template for mustache processing capabilities
 * @since 1.0.0
 */
@Slf4j
@Getter
public class GrafanaConstruct extends GrafanaBaseConstruct {
  private final HelmChart chart;

  /**
   * Creates a new GrafanaConstruct with k8s-monitoring Helm chart deployment.
   *
   * <p>
   * This constructor validates Grafana Cloud context, processes the mustache template with context values, and deploys the k8s-monitoring
   * Helm chart with proper configuration for metrics, logs, traces, and profiling integration with Grafana Cloud.
   *
   * @param scope
   *          the CDK construct scope for this construct
   * @param common
   *          common configuration values including cluster identification
   * @param conf
   *          Grafana addon configuration including chart details and values template
   * @param cluster
   *          the target EKS cluster for Helm chart deployment
   * @throws RuntimeException
   *           if template processing fails or chart deployment encounters errors
   */
  @SneakyThrows
  public GrafanaConstruct(Construct scope, Common common, GrafanaAddon conf, ICluster cluster) {
    super(scope, id("grafana", conf.chart().release()));

    log.debug("{} [common: {} conf: {}]", "GrafanaConstruct", common, conf);

    var secret = createSecretFromContext(scope);
    if (secret == null) {
      this.chart = null;
      return;
    }

    var parsed = Template.parse(scope, conf.chart().values(),
      Map.ofEntries(Map.entry("hosted:eks:grafana:key", secret.key()), Map.entry("hosted:eks:grafana:instanceId", secret.instanceId()),
        Map.entry("hosted:eks:grafana:lokiHost", secret.lokiHost()), Map.entry("hosted:eks:grafana:lokiUsername", secret.lokiUsername()),
        Map.entry("hosted:eks:grafana:prometheusHost", secret.prometheusHost()),
        Map.entry("hosted:eks:grafana:prometheusUsername", secret.prometheusUsername()),
        Map.entry("hosted:eks:grafana:tempoHost", secret.tempoHost()),
        Map.entry("hosted:eks:grafana:tempoUsername", secret.tempoUsername()),
        Map.entry("hosted:eks:grafana:pyroscopeHost", secret.pyroscopeHost())));

    var values = Mapper.get().readValue(parsed, new TypeReference<Map<String, Object>>() {});
    this.chart = HelmChart.Builder.create(this, conf.chart().name()).cluster(cluster).wait(true).timeout(Duration.minutes(15))
      .skipCrds(false).createNamespace(true).chart(conf.chart().name()).namespace(conf.chart().namespace())
      .repository(conf.chart().repository()).release(conf.chart().release()).version(conf.chart().version()).values(values).build();
  }
}
