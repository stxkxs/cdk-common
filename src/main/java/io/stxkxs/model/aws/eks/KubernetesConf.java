package io.stxkxs.model.aws.eks;

import java.util.List;
import java.util.Map;

/**
 * Comprehensive Amazon EKS (Elastic Kubernetes Service) cluster configuration record that
 * defines complete Kubernetes cluster setup with advanced networking, security, and operational features.
 * 
 * <p>This configuration orchestrates all aspects of EKS cluster provisioning including control plane
 * configuration, worker node management, add-on ecosystem, security policies, and observability
 * infrastructure for production-ready Kubernetes deployments.
 * 
 * <p><b>Core Cluster Configuration:</b>
 * <ul>
 *   <li><b>Cluster Identity</b> - Name and version specification for EKS cluster</li>
 *   <li><b>Endpoint Access</b> - Public, private, or hybrid API server access control</li>
 *   <li><b>Logging Types</b> - CloudWatch log categories (api, audit, authenticator, controllerManager, scheduler)</li>
 *   <li><b>Resource Pruning</b> - Automatic cleanup of unused Kubernetes resources</li>
 * </ul>
 * 
 * <p><b>Networking and Infrastructure:</b>
 * <ul>
 *   <li><b>VPC Integration</b> - Subnet type specification for cluster placement</li>
 *   <li><b>Multi-AZ Support</b> - Cross-availability zone deployment patterns</li>
 *   <li><b>Network Policies</b> - Pod-to-pod communication security rules</li>
 *   <li><b>Service Mesh</b> - Advanced networking and traffic management</li>
 * </ul>
 * 
 * <p><b>Security and Access Control:</b>
 * <ul>
 *   <li><b>RBAC Configuration</b> - Role-based access control policies and bindings</li>
 *   <li><b>Tenancy Model</b> - Multi-tenant access patterns and isolation</li>
 *   <li><b>Pod Security</b> - Security contexts and admission controllers</li>
 *   <li><b>Network Security</b> - Security groups and NACLs integration</li>
 * </ul>
 * 
 * <p><b>Operational Components:</b>
 * <ul>
 *   <li><b>Node Groups</b> - Worker node configuration and auto-scaling policies</li>
 *   <li><b>Add-ons Ecosystem</b> - Managed and custom Kubernetes add-ons</li>
 *   <li><b>Observability Stack</b> - Monitoring, logging, and alerting infrastructure</li>
 *   <li><b>Message Queuing</b> - SQS integration for event processing and notifications</li>
 * </ul>
 * 
 * <p><b>Template-Based Configuration:</b>
 * Most configuration fields accept JSON templates that support dynamic value injection:
 * <ul>
 *   <li><b>Environment Variables</b> - Runtime environment configuration</li>
 *   <li><b>Resource ARNs</b> - Cross-service resource references</li>
 *   <li><b>Context Injection</b> - CDK context and metadata integration</li>
 *   <li><b>Conditional Logic</b> - Environment-specific configuration branching</li>
 * </ul>
 * 
 * <p><b>Complex Integration Patterns:</b>
 * This configuration supports sophisticated deployment scenarios including:
 * <ul>
 *   <li>Multi-region cluster federation</li>
 *   <li>Hybrid on-premises and cloud workloads</li>
 *   <li>GitOps-based continuous deployment</li>
 *   <li>Service mesh integration (Istio, Linkerd)</li>
 *   <li>Advanced observability with Prometheus, Grafana, and distributed tracing</li>
 * </ul>
 * 
 * <p><b>Kubernetes Resource Management:</b>
 * <ul>
 *   <li><b>Annotations</b> - Kubernetes metadata for operational tools</li>
 *   <li><b>Labels</b> - Selector-based resource organization and scheduling</li>
 *   <li><b>Tags</b> - AWS resource tagging for billing and management</li>
 * </ul>
 * 
 * <p><b>Usage Example:</b>
 * <pre>{@code
 * KubernetesConf config = new KubernetesConf(
 *     "production-cluster",                    // cluster name
 *     "1.28",                                  // Kubernetes version
 *     "PUBLIC_AND_PRIVATE",                    // endpoint access
 *     true,                                    // enable pruning
 *     rbacConfigJson,                          // RBAC policies
 *     tenancyConfigJson,                       // tenant configuration
 *     List.of("api", "audit", "scheduler"),    // logging categories
 *     List.of("PRIVATE", "PUBLIC"),            // subnet types
 *     nodeGroupsConfigJson,                    // worker nodes
 *     addonsConfigJson,                        // Kubernetes add-ons
 *     sqsConfigJson,                           // message queuing
 *     observabilityConfigJson,                 // monitoring stack
 *     clusterAnnotations,                      // Kubernetes annotations
 *     clusterLabels,                          // Kubernetes labels
 *     awsTags                                 // AWS resource tags
 * );
 * }</pre>
 * 
 * @param name Unique identifier for the EKS cluster
 * @param version Kubernetes version for the cluster control plane
 * @param endpointAccess API server endpoint access configuration (PUBLIC, PRIVATE, PUBLIC_AND_PRIVATE)
 * @param prune Enable automatic pruning of unused Kubernetes resources
 * @param rbac JSON configuration for role-based access control policies
 * @param tenancy JSON configuration for multi-tenant access and isolation
 * @param loggingTypes List of CloudWatch log categories to enable
 * @param vpcSubnetTypes List of VPC subnet types for cluster placement
 * @param nodeGroups JSON configuration for managed node groups and auto-scaling
 * @param addons JSON configuration for managed and custom Kubernetes add-ons
 * @param sqs JSON configuration for SQS integration and event processing
 * @param observability JSON configuration for monitoring, logging, and alerting
 * @param annotations Kubernetes annotations applied to cluster resources
 * @param labels Kubernetes labels for resource selection and organization
 * @param tags AWS resource tags for billing, management, and governance
 * 
 * @author CDK Common Framework
 * @since 1.0.0
 * @see NodeGroup for worker node configuration details
 * @see AddonsConf for add-on ecosystem configuration
 * @see ObservabilityConf for monitoring stack setup
 * @see TenancyConf for multi-tenant access patterns
 */
public record KubernetesConf(
  String name,
  String version,
  String endpointAccess,
  boolean prune,
  String rbac,
  String tenancy,
  List<String> loggingTypes,
  List<String> vpcSubnetTypes,
  String nodeGroups,
  String addons,
  String sqs,
  String observability,
  Map<String, String> annotations,
  Map<String, String> labels,
  Map<String, String> tags
) {}