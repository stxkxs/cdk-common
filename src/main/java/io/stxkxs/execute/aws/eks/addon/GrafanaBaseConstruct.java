package io.stxkxs.execute.aws.eks.addon;

import io.stxkxs.model.aws.eks.addon.core.GrafanaSecret;
import lombok.extern.slf4j.Slf4j;
import software.constructs.Construct;

/**
 * Base class for Grafana-related constructs that require Grafana Cloud context validation.
 * 
 * <p>This base class provides shared functionality for constructs that depend on
 * Grafana Cloud configuration being present in the CDK context. It handles:
 * 
 * <ul>
 *   <li><b>Context Validation</b> - Validates all required Grafana context values are present</li>
 *   <li><b>Secret Creation</b> - Creates GrafanaSecret from validated context values</li>
 *   <li><b>Graceful Degradation</b> - Allows constructs to fail gracefully when context is missing</li>
 * </ul>
 * 
 * <p><b>Required Context Keys:</b>
 * <ul>
 *   <li>hosted:eks:grafana:key</li>
 *   <li>hosted:eks:grafana:instanceId</li>
 *   <li>hosted:eks:grafana:lokiHost</li>
 *   <li>hosted:eks:grafana:lokiUsername</li>
 *   <li>hosted:eks:grafana:prometheusHost</li>
 *   <li>hosted:eks:grafana:prometheusUsername</li>
 *   <li>hosted:eks:grafana:tempoHost</li>
 *   <li>hosted:eks:grafana:tempoUsername</li>
 *   <li>hosted:eks:grafana:pyroscopeHost</li>
 * </ul>
 */
@Slf4j
public abstract class GrafanaBaseConstruct extends Construct {

  protected GrafanaBaseConstruct(Construct scope, String id) {
    super(scope, id);
  }

  /**
   * Creates a GrafanaSecret from CDK context values.
   * Returns null if any required context values are missing.
   */
  protected static GrafanaSecret createSecretFromContext(Construct scope) {
    try {
      var key = getContextValue(scope, "hosted:eks:grafana:key");
      var instanceId = getContextValue(scope, "hosted:eks:grafana:instanceId");
      var lokiHost = getContextValue(scope, "hosted:eks:grafana:lokiHost");
      var lokiUsername = getContextValue(scope, "hosted:eks:grafana:lokiUsername");
      var prometheusHost = getContextValue(scope, "hosted:eks:grafana:prometheusHost");
      var prometheusUsername = getContextValue(scope, "hosted:eks:grafana:prometheusUsername");
      var tempoHost = getContextValue(scope, "hosted:eks:grafana:tempoHost");
      var tempoUsername = getContextValue(scope, "hosted:eks:grafana:tempoUsername");
      var pyroscopeHost = getContextValue(scope, "hosted:eks:grafana:pyroscopeHost");

      if (key == null || instanceId == null || lokiHost == null || lokiUsername == null ||
          prometheusHost == null || prometheusUsername == null || tempoHost == null ||
          tempoUsername == null || pyroscopeHost == null) {
        log.warn("missing required grafana context values, skipping grafana-related deployments");
        return null;
      }

      return new GrafanaSecret(
        key,
        lokiHost,
        lokiUsername,
        prometheusHost,
        prometheusUsername,
        tempoHost,
        tempoUsername,
        instanceId,
        pyroscopeHost
      );
    } catch (Exception e) {
      log.error("failed to create grafana secret from context: {}", e.getMessage(), e);
      return null;
    }
  }

  /**
   * Retrieves a context value as a non-empty string, or returns null.
   */
  protected static String getContextValue(Construct scope, String key) {
    var value = scope.getNode().tryGetContext(key);
    if (value instanceof String stringValue && !stringValue.isEmpty()) {
      return stringValue;
    }
    return null;
  }
}