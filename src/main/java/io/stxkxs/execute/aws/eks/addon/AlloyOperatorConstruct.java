package io.stxkxs.execute.aws.eks.addon;

import com.fasterxml.jackson.core.type.TypeReference;
import io.stxkxs.execute.serialization.Mapper;
import io.stxkxs.execute.serialization.Template;
import io.stxkxs.model._main.Common;
import io.stxkxs.model.aws.eks.addon.core.AlloyOperatorAddon;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.services.eks.HelmChart;
import software.amazon.awscdk.services.eks.ICluster;
import software.constructs.Construct;

import java.util.Map;

import static io.stxkxs.execute.serialization.Format.id;

/**
 * Grafana Alloy Operator Helm chart deployment construct for Kubernetes observability infrastructure.
 * 
 * <p>This construct deploys the Grafana Alloy Operator, which is required for the k8s-monitoring
 * chart v3+ to function properly. The operator manages Alloy instances as Custom Resources and
 * provides the necessary CRDs and infrastructure.
 * 
 * <p><b>Core Capabilities:</b>
 * <ul>
 *   <li><b>Alloy CRD Management</b> - Installs and manages Alloy Custom Resource Definitions</li>
 *   <li><b>Operator Deployment</b> - Deploys the Alloy Operator controller for managing Alloy instances</li>
 *   <li><b>Context Validation</b> - Validates Grafana Cloud context before deployment via base class</li>
 *   <li><b>Dependency Management</b> - Ensures proper deployment order with k8s-monitoring chart</li>
 * </ul>
 * 
 * <p><b>Dependencies and Requirements:</b>
 * <ul>
 *   <li><b>Grafana Context</b> - Requires valid Grafana Cloud configuration in CDK context</li>
 *   <li><b>EKS Cluster</b> - Must be deployed to an existing EKS cluster</li>
 *   <li><b>k8s-monitoring Prerequisite</b> - Required for k8s-monitoring chart v3.0+</li>
 * </ul>
 * 
 * <p><b>Deployment Configuration:</b>
 * <ul>
 *   <li><b>Namespace</b> - Deployed to alloy-system namespace</li>
 *   <li><b>Chart Version</b> - Uses Grafana's official alloy-operator Helm chart</li>
 *   <li><b>Resource Limits</b> - Configured with appropriate CPU and memory limits</li>
 *   <li><b>Timeout</b> - 5-minute deployment timeout for operator readiness</li>
 * </ul>
 * 
 * <p><b>Integration with Grafana Stack:</b>
 * The Alloy Operator is a critical component of the modern Grafana observability stack:
 * <pre>
 * AlloyOperator → installs CRDs → k8s-monitoring deploys → Alloy instances collect telemetry
 *      ↓                ↓                    ↓                         ↓
 *  Operator Pod    Custom Resources    Helm Chart Success      Metrics/Logs/Traces
 * </pre>
 * 
 * <p><b>Usage Example:</b>
 * <pre>{@code
 * AlloyOperatorConstruct alloyOperator = new AlloyOperatorConstruct(
 *     this, common, alloyOperatorConfig, cluster);
 * 
 * // Automatically handles:
 * // - Grafana context validation from base class
 * // - Alloy Operator Helm chart deployment
 * // - CRD installation for k8s-monitoring compatibility
 * // - Graceful skipping if Grafana context is missing
 * }</pre>
 * 
 * @author CDK Common Framework
 * @see GrafanaBaseConstruct for context validation functionality
 * @see GrafanaConstruct for the main k8s-monitoring integration
 * @see HelmChart for Kubernetes deployment mechanism
 * @since 1.0.0
 */
@Slf4j
@Getter
public class AlloyOperatorConstruct extends GrafanaBaseConstruct {
  private final HelmChart chart;

  /**
   * Creates a new AlloyOperatorConstruct with Alloy Operator Helm chart deployment.
   * 
   * <p>This constructor validates Grafana Cloud context (inherited from base class), processes
   * the mustache template for operator configuration, and deploys the alloy-operator Helm chart
   * which provides the necessary CRDs and operator for k8s-monitoring chart v3+ compatibility.
   * 
   * <p>The deployment will be skipped gracefully if Grafana Cloud context is not properly
   * configured, ensuring that the entire observability stack deploys as a cohesive unit.
   * 
   * @param scope the CDK construct scope for this construct
   * @param common common configuration values including cluster identification
   * @param conf Alloy Operator addon configuration including chart details and values template
   * @param cluster the target EKS cluster for Helm chart deployment
   * 
   * @throws RuntimeException if template processing fails or chart deployment encounters errors
   */
  @SneakyThrows
  public AlloyOperatorConstruct(Construct scope, Common common, AlloyOperatorAddon conf, ICluster cluster) {
    super(scope, id("alloy-operator", conf.chart().release()));

    log.debug("{} [common: {} conf: {}]", "AlloyOperatorConstruct", common, conf);

    var secret = createSecretFromContext(scope);
    if (secret == null) {
      this.chart = null;
      return;
    }

    var parsed = Template.parse(scope, conf.chart().values());
    var values = Mapper.get().readValue(parsed, new TypeReference<Map<String, Object>>() {});
    this.chart = HelmChart.Builder
      .create(this, conf.chart().name())
      .cluster(cluster)
      .wait(true)
      .timeout(Duration.minutes(5))
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