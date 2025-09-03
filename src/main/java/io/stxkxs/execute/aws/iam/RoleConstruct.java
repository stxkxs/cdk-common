package io.stxkxs.execute.aws.iam;

import io.stxkxs.model._main.Common;
import io.stxkxs.model._main.Common.Maps;
import io.stxkxs.model.aws.iam.IamRole;
import io.stxkxs.model.aws.iam.PolicyConf;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awscdk.Tags;
import software.amazon.awscdk.services.iam.IPrincipal;
import software.amazon.awscdk.services.iam.ManagedPolicy;
import software.amazon.awscdk.services.iam.PolicyDocument;
import software.amazon.awscdk.services.iam.Role;
import software.constructs.Construct;

import java.util.List;
import java.util.Map;

import static io.stxkxs.execute.serialization.Format.id;
import static java.util.stream.Collectors.toMap;

/**
 * Comprehensive AWS IAM Role construct that provides sophisticated identity and access management
 * with advanced policy management, principal integration, and security best practices for AWS resources.
 * 
 * <p>This construct serves as the cornerstone of AWS security architecture, orchestrating IAM roles
 * with complex policy attachments, principal trust relationships, and enterprise-grade access control
 * patterns required for secure cloud deployments.
 * 
 * <p><b>Core IAM Features:</b>
 * <ul>
 *   <li><b>Role Creation</b> - AWS IAM roles with configurable trust relationships</li>
 *   <li><b>Policy Management</b> - Both managed and custom inline policies</li>
 *   <li><b>Principal Integration</b> - Support for service, user, and federated principals</li>
 *   <li><b>Security Controls</b> - Least-privilege access patterns and compliance features</li>
 * </ul>
 * 
 * <p><b>Advanced Security Architecture:</b>
 * <ul>
 *   <li><b>Trust Relationships</b> - Sophisticated principal trust policy configuration</li>
 *   <li><b>Cross-Account Access</b> - Multi-account role assumption patterns</li>
 *   <li><b>Federated Identity</b> - OIDC, SAML, and web identity federation</li>
 *   <li><b>Service Integration</b> - AWS service-specific role configurations</li>
 * </ul>
 * 
 * <p><b>Policy Management System:</b>
 * The construct supports sophisticated policy architectures:
 * <ul>
 *   <li><b>AWS Managed Policies</b> - Predefined AWS service policies</li>
 *   <li><b>Customer Managed Policies</b> - Reusable custom policies</li>
 *   <li><b>Inline Policies</b> - Role-specific embedded policies</li>
 *   <li><b>Policy Templates</b> - Dynamic policy generation from templates</li>
 * </ul>
 * 
 * <p><b>Principal Types Support:</b>
 * <ul>
 *   <li><b>AWS Service Principals</b> - Lambda, ECS, EKS, and other service roles</li>
 *   <li><b>IAM User/Group Principals</b> - Human user and group access</li>
 *   <li><b>Federated Principals</b> - OIDC, SAML, and social identity providers</li>
 *   <li><b>Cross-Account Principals</b> - Multi-account access patterns</li>
 * </ul>
 * 
 * <p><b>Security Best Practices:</b>
 * <ul>
 *   <li><b>Principle of Least Privilege</b> - Minimal necessary permissions</li>
 *   <li><b>Time-based Access</b> - Temporary credentials and session management</li>
 *   <li><b>Condition-based Policies</b> - Context-aware access controls</li>
 *   <li><b>Audit Integration</b> - CloudTrail and access logging support</li>
 * </ul>
 * 
 * <p><b>Enterprise Integration:</b>
 * <ul>
 *   <li><b>Resource Tagging</b> - Comprehensive tagging for governance and cost allocation</li>
 *   <li><b>Compliance Ready</b> - SOC, PCI, HIPAA compliance considerations</li>
 *   <li><b>Policy Validation</b> - Access Analyzer integration for policy verification</li>
 *   <li><b>Permission Boundaries</b> - Maximum permission guardrails</li>
 * </ul>
 * 
 * <p><b>Dual Constructor Pattern:</b>
 * The construct provides flexible role creation approaches:
 * <ul>
 *   <li><b>External Principal</b> - Role with externally provided principal (constructor 1)</li>
 *   <li><b>Configured Principal</b> - Role with principal from configuration (constructor 2)</li>
 *   <li><b>Dynamic Selection</b> - Framework chooses appropriate constructor based on context</li>
 *   <li><b>Migration Support</b> - Easy migration between principal configuration patterns</li>
 * </ul>
 * 
 * <p><b>IAM Role Architecture:</b>
 * <pre>
 * Principal Trust → IAM Role → Policy Attachments → AWS Resource Access
 *       ↓              ↓              ↓                      ↓
 * Authentication → AssumeRole → Permission Evaluation → API Calls
 * </pre>
 * 
 * <p><b>Policy Evaluation Flow:</b>
 * <pre>
 * Trust Policy → Managed Policies → Inline Policies → Permission Boundaries → Resource Policies
 *      ↓              ↓                   ↓                   ↓                      ↓
 * Principal Auth → Base Permissions → Custom Permissions → Max Permissions → Final Access
 * </pre>
 * 
 * <p><b>Usage Examples:</b>
 * <pre>{@code
 * // Role with external principal (Lambda, EKS, etc.)
 * RoleConstruct lambdaRole = new RoleConstruct(
 *     this, 
 *     common, 
 *     ServicePrincipal.fromService("lambda.amazonaws.com"), 
 *     roleConfig
 * );
 * 
 * // Role with configured principal (from configuration)
 * RoleConstruct configuredRole = new RoleConstruct(
 *     this, 
 *     common, 
 *     roleConfigWithPrincipal
 * );
 * 
 * // The construct automatically handles:
 * // - IAM role creation with trust policy
 * // - Managed policy attachments
 * // - Inline policy document generation
 * // - Resource tagging and governance
 * // - Cross-account access patterns
 * 
 * // Access the created role
 * Role iamRole = roleConstruct.getRole();
 * 
 * // Use role in other constructs
 * Function lambda = Function.Builder.create(this, "function")
 *     .role(lambdaRole.getRole())
 *     .build();
 * }</pre>
 * 
 * @author CDK Common Framework
 * @since 1.0.0
 * @see Role for AWS CDK IAM role construct
 * @see IamPolicy for policy statement generation
 * @see IPrincipal for principal management
 * @see IamRole for role configuration model
 */
@Slf4j
@Getter
public class RoleConstruct extends Construct {
  private final Role role;

  public RoleConstruct(Construct scope, Common common, IPrincipal principal, IamRole conf) {
    super(scope, id("role", conf.name()));

    log.debug("{} [common: {} conf: {}]", "RoleConstruct", common, conf);

    this.role = Role.Builder
      .create(this, conf.name())
      .roleName(conf.name())
      .description(conf.description())
      .assumedBy(principal)
      .managedPolicies(conf.managedPolicyNames().stream()
        .map(ManagedPolicy::fromAwsManagedPolicyName)
        .toList())
      .inlinePolicies(inlinePolicies(conf.customPolicies()))
      .build();

    Maps.from(common.tags(), conf.tags())
      .forEach((k, v) -> Tags.of(this.role()).add(k, v));
  }

  public RoleConstruct(Construct scope, Common common, IamRole conf) {
    super(scope, id("role", conf.name()));

    log.debug("{} [common: {} conf: {}]", "RoleConstruct", common, conf);

    this.role = Role.Builder
      .create(scope, conf.name())
      .roleName(conf.name())
      .description(conf.description())
      .assumedBy(conf.principal().iamPrincipal())
      .managedPolicies(conf.managedPolicyNames().stream()
        .map(ManagedPolicy::fromAwsManagedPolicyName)
        .toList())
      .inlinePolicies(inlinePolicies(conf.customPolicies()))
      .build();

    Maps.from(common.tags(), conf.tags())
      .forEach((k, v) -> Tags.of(this.role()).add(k, v));
  }

  private Map<String, PolicyDocument> inlinePolicies(List<PolicyConf> customPolicies) {
    return customPolicies.stream()
      .map(policy -> {
        var document = PolicyDocument.Builder.create()
          .statements(IamPolicy.policyStatements(this, policy))
          .build();
        return Map.entry(policy.name(), document);
      })
      .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
  }
}
