package io.stxkxs.model.aws.iam;

import io.stxkxs.model.aws.eks.PodIdentity;
import io.stxkxs.model.aws.eks.ServiceAccountConf;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awscdk.CfnJson;
import software.amazon.awscdk.services.iam.AccountPrincipal;
import software.amazon.awscdk.services.iam.ArnPrincipal;
import software.amazon.awscdk.services.iam.CompositePrincipal;
import software.amazon.awscdk.services.iam.FederatedPrincipal;
import software.amazon.awscdk.services.iam.IOpenIdConnectProvider;
import software.amazon.awscdk.services.iam.IPrincipal;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.amazon.awscdk.services.iam.StarPrincipal;
import software.amazon.awscdk.services.iam.WebIdentityPrincipal;
import software.constructs.Construct;

/**
 * Comprehensive IAM Principal configuration record that provides sophisticated AWS identity and access management with support for multiple
 * principal types and complex trust relationships.
 *
 * <p>
 * This record serves as the foundational component for IAM role creation, policy attachment, and trust relationship establishment across
 * the entire CDK infrastructure framework, with specialized support for EKS OIDC integration and composite principal scenarios.
 *
 * <p>
 * <b>Supported Principal Types:</b>
 * <ul>
 * <li><b>AWS Account</b> - Cross-account access via AccountPrincipal</li>
 * <li><b>Federated</b> - SAML and OIDC federated identity integration</li>
 * <li><b>ARN</b> - Direct ARN-based principal specification</li>
 * <li><b>Service</b> - AWS service principals (lambda.amazonaws.com, etc.)</li>
 * <li><b>Star (*)</b> - Public access principal for open policies</li>
 * </ul>
 *
 * <p>
 * <b>Advanced Features:</b>
 * <ul>
 * <li><b>Composite Principals</b> - Multiple principals combined in single policy</li>
 * <li><b>Conditional Access</b> - IAM policy conditions for fine-grained control</li>
 * <li><b>OIDC Integration</b> - EKS service account and pod identity trust relationships</li>
 * <li><b>Session Tagging</b> - Automatic session tag support for federated access</li>
 * </ul>
 *
 * <p>
 * <b>EKS OIDC Specialization:</b> The record provides specialized methods for EKS OpenID Connect integration:
 * <ul>
 * <li><b>Service Account Binding</b> - Maps Kubernetes service accounts to IAM roles</li>
 * <li><b>Pod Identity</b> - Direct pod-to-IAM role association</li>
 * <li><b>Audience Validation</b> - STS audience verification for security</li>
 * <li><b>Namespace Isolation</b> - Kubernetes namespace-based access control</li>
 * </ul>
 *
 * <p>
 * <b>Composite Principal Architecture:</b> When multiple principals need identical permissions, the composite functionality combines them
 * into a single CompositePrincipal, reducing policy complexity and improving maintainability while preserving fine-grained access control.
 *
 * <p>
 * <b>Security Considerations:</b>
 * <ul>
 * <li>Condition-based access control for enhanced security</li>
 * <li>OIDC issuer validation for EKS integration</li>
 * <li>Proper audience restriction for token validation</li>
 * <li>Namespace-based isolation for multi-tenant environments</li>
 * </ul>
 *
 * <p>
 * <b>Trust Relationship Patterns:</b>
 *
 * <pre>
 * Account Trust    →    AccountPrincipal    →    Cross-account access
 * Service Trust    →    ServicePrincipal    →    AWS service access
 * OIDC Trust      →    WebIdentityPrincipal →    EKS workload access
 * Federated Trust →    FederatedPrincipal  →    SAML/external IDP
 * </pre>
 *
 * <p>
 * <b>Usage Examples:</b>
 *
 * <pre>{@code
 * // Simple service principal
 * Principal lambdaPrincipal = Principal.builder().type(PrincipalType.SERVICE).value("lambda.amazonaws.com").build();
 *
 * // EKS service account with OIDC
 * IPrincipal eksPrincipal = principal.oidcPrincipal(scope, oidcProvider, serviceAccountConfig);
 *
 * // Composite principal for multiple access patterns
 * Principal composite = Principal.builder().type(PrincipalType.SERVICE).value("lambda.amazonaws.com")
 *   .composite(List.of(ec2Principal, fargatePrincipal)).conditions(Map.of("StringEquals", conditionMap)).build();
 * }</pre>
 *
 * @param type
 *          The type of principal (AWS, SERVICE, FEDERATED, ARN, STAR)
 * @param value
 *          The principal identifier (account ID, service name, ARN, etc.)
 * @param action
 *          The federated action for assume role operations
 * @param composite
 *          List of additional principals to combine with the primary principal
 * @param conditions
 *          IAM policy conditions for conditional access control
 * @author CDK Common Framework
 * @see PrincipalType for supported principal type enumeration
 * @see IPrincipal for AWS CDK principal interface implementation
 * @see ServiceAccountConf for EKS service account configuration
 * @see PodIdentity for EKS pod identity configuration
 * @since 1.0.0
 */
@Builder
@Slf4j
public record Principal(PrincipalType type, String value, String action, List<Principal> composite, Map<String, Object> conditions) {

  public IPrincipal iamPrincipal() {
    if (type.equals(PrincipalType.AWS) || type.equals(PrincipalType.ACCOUNT)) {
      var principal = new AccountPrincipal(value()).withConditions(this.conditions());
      return maybeComposite(principal);
    } else if (type.equals(PrincipalType.FEDERATED)) {
      var principal = new FederatedPrincipal(value(), this.conditions(), this.action()).withSessionTags();
      return maybeComposite(principal);
    } else if (type.equals(PrincipalType.ARN)) {
      var principal = new ArnPrincipal(value()).withConditions(this.conditions());
      return maybeComposite(principal);
    } else if (type.equals(PrincipalType.SERVICE)) {
      var principal = new ServicePrincipal(value()).withConditions(this.conditions());
      return maybeComposite(principal);
    } else if (type.equals(PrincipalType.STAR)) {
      var principal = new StarPrincipal().withConditions(this.conditions());
      return maybeComposite(principal);
    } else {
      throw new IllegalStateException("unknown principal type " + value());
    }
  }

  public IPrincipal maybeComposite(IPrincipal principal) {
    if (composite == null || composite.isEmpty()) {
      return principal;
    }

    var p = Stream.concat(Stream.of(principal), composite.stream().map(Principal::iamPrincipal)).toList();

    var compositePrincipal = new CompositePrincipal(p.toArray(IPrincipal[]::new));
    if (conditions() != null) {
      compositePrincipal.withConditions(conditions());
    }

    return compositePrincipal;
  }

  public IPrincipal oidcPrincipal(Construct scope, IOpenIdConnectProvider oidc, ServiceAccountConf sa) {
    return new WebIdentityPrincipal(oidc.getOpenIdConnectProviderArn()).withConditions(Map.of("StringEquals",
      CfnJson.Builder.create(scope, String.format("oidc-principal-%s", sa.role().name()))
        .value(Map.of(String.format("%s:aud", oidc.getOpenIdConnectProviderIssuer()), "sts.amazonaws.com",
          String.format("%s:sub", oidc.getOpenIdConnectProviderIssuer()),
          String.format("system:serviceaccount:%s:%s", sa.metadata().getNamespace(), sa.metadata().getName())))
        .build()));
  }

  public IPrincipal oidcPrincipal(Construct scope, IOpenIdConnectProvider oidc, PodIdentity pod) {
    return new WebIdentityPrincipal(oidc.getOpenIdConnectProviderArn()).withConditions(Map.of("StringEquals",
      CfnJson.Builder.create(scope, String.format("oidc-principal-%s", pod.role().name()))
        .value(Map.of(String.format("%s:aud", oidc.getOpenIdConnectProviderIssuer()), "sts.amazonaws.com",
          String.format("%s:sub", oidc.getOpenIdConnectProviderIssuer()),
          String.format("system:serviceaccount:%s:%s", pod.metadata().getNamespace(), pod.metadata().getName())))
        .build()));
  }
}
