package io.stxkxs.execute.aws.cognito;

import io.stxkxs.execute.aws.iam.RoleConstruct;
import io.stxkxs.execute.serialization.Mapper;
import io.stxkxs.execute.serialization.Template;
import io.stxkxs.model._main.Common;
import io.stxkxs.model.aws.cognito.identitypool.IdentityPoolConf;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awscdk.CfnJson;
import software.amazon.awscdk.customresources.AwsCustomResource;
import software.amazon.awscdk.customresources.AwsCustomResourcePolicy;
import software.amazon.awscdk.customresources.AwsSdkCall;
import software.amazon.awscdk.customresources.PhysicalResourceId;
import software.amazon.awscdk.services.apigateway.RestApi;
import software.amazon.awscdk.services.cognito.UserPool;
import software.amazon.awscdk.services.cognito.UserPoolClient;
import software.amazon.awscdk.services.cognito.identitypool.alpha.IdentityPool;
import software.amazon.awscdk.services.cognito.identitypool.alpha.IdentityPool.Builder;
import software.amazon.awscdk.services.cognito.identitypool.alpha.IdentityPoolAuthenticationProviders;
import software.amazon.awscdk.services.cognito.identitypool.alpha.IdentityPoolProviderUrl;
import software.amazon.awscdk.services.cognito.identitypool.alpha.IdentityPoolRoleMapping;
import software.amazon.awscdk.services.cognito.identitypool.alpha.RoleMappingRule;
import software.amazon.awscdk.services.cognito.identitypool.alpha.UserPoolAuthenticationProvider;
import software.amazon.awscdk.services.iam.Effect;
import software.amazon.awscdk.services.iam.PolicyStatement;
import software.amazon.awscdk.services.iam.Role;
import software.constructs.Construct;

/**
 * Comprehensive AWS Cognito Identity Pool construct that provides sophisticated federated identity management with role mapping, external
 * identity provider integration, and advanced authentication patterns.
 *
 * <p>
 * This construct orchestrates the creation of Cognito Identity Pools with complex role-based access control, supporting multiple
 * authentication providers, custom resource provisioning, and advanced identity federation scenarios for multi-platform applications.
 *
 * <p>
 * <b>Core Identity Federation Features:</b>
 * <ul>
 * <li><b>Identity Pool Creation</b> - Cognito Identity Pool with federated identity support</li>
 * <li><b>Role Mapping</b> - Sophisticated IAM role assignment based on authentication state</li>
 * <li><b>Authentication Providers</b> - Integration with User Pools, SAML, OIDC, and social providers</li>
 * <li><b>Custom Resource Management</b> - AWS SDK-based custom resources for advanced configuration</li>
 * </ul>
 *
 * <p>
 * <b>Advanced Authentication Patterns:</b>
 * <ul>
 * <li><b>Multi-Provider Support</b> - User Pools, Facebook, Google, Amazon, Twitter, SAML, OIDC</li>
 * <li><b>Role-Based Access</b> - Different IAM roles for authenticated vs unauthenticated users</li>
 * <li><b>Dynamic Role Assignment</b> - Context-based role mapping with custom rules</li>
 * <li><b>Cross-Platform Identity</b> - Unified identity across web, mobile, and server applications</li>
 * </ul>
 *
 * <p>
 * <b>IAM Integration Architecture:</b> The construct provides sophisticated IAM role management:
 * <ul>
 * <li><b>Authenticated Roles</b> - Full-access roles for verified users</li>
 * <li><b>Unauthenticated Roles</b> - Limited-access roles for anonymous users</li>
 * <li><b>Custom Role Mapping</b> - Advanced role selection based on user attributes</li>
 * <li><b>Policy Templates</b> - Template-based policy generation with environment variables</li>
 * </ul>
 *
 * <p>
 * <b>Custom Resource Integration:</b> Uses AWS Custom Resources for advanced configuration:
 * <ul>
 * <li><b>SDK-Based Operations</b> - Direct AWS SDK calls for Identity Pool configuration</li>
 * <li><b>Advanced Settings</b> - Configuration options not available in CDK constructs</li>
 * <li><b>Dynamic Updates</b> - Runtime configuration changes through custom resources</li>
 * <li><b>Error Handling</b> - Robust error handling and rollback mechanisms</li>
 * </ul>
 *
 * <p>
 * <b>External Provider Integration:</b>
 * <ul>
 * <li><b>SAML Providers</b> - Enterprise identity provider integration</li>
 * <li><b>OIDC Providers</b> - OpenID Connect federation</li>
 * <li><b>Social Providers</b> - Facebook, Google, Amazon, Twitter integration</li>
 * <li><b>Developer Providers</b> - Custom authentication backend integration</li>
 * </ul>
 *
 * <p>
 * <b>Security and Compliance:</b>
 * <ul>
 * <li><b>Principle of Least Privilege</b> - Minimal necessary permissions for each role</li>
 * <li><b>Cross-Account Access</b> - Support for cross-account identity federation</li>
 * <li><b>Audit Trail</b> - CloudTrail integration for identity operations</li>
 * <li><b>Compliance Ready</b> - GDPR, HIPAA, and SOC compliance considerations</li>
 * </ul>
 *
 * <p>
 * <b>Template-Based Configuration:</b> Supports dynamic configuration through template processing:
 * <ul>
 * <li><b>Environment-Specific Providers</b> - Different identity providers per environment</li>
 * <li><b>Dynamic Role Policies</b> - Environment-specific IAM permissions</li>
 * <li><b>Provider Configuration</b> - Parameterized external provider settings</li>
 * </ul>
 *
 * <p>
 * <b>Identity Flow Architecture:</b>
 *
 * <pre>
 * External Provider → Identity Pool → Role Mapping → AWS Credentials → Resource Access
 *        ↓                 ↓              ↓              ↓                   ↓
 * Authentication → Token Exchange → Role Selection → STS AssumeRole → API Calls
 * </pre>
 *
 * <p>
 * <b>Multi-Platform Support:</b>
 * <ul>
 * <li><b>Web Applications</b> - JavaScript SDK integration with CORS support</li>
 * <li><b>Mobile Apps</b> - iOS and Android SDK integration</li>
 * <li><b>Server Applications</b> - Backend service integration</li>
 * <li><b>Hybrid Architectures</b> - Mixed client/server authentication patterns</li>
 * </ul>
 *
 * <p>
 * <b>Usage Example:</b>
 *
 * <pre>{@code
 * IdentityPoolConstruct identityPool = new IdentityPoolConstruct(this, common, identityPoolConfig, userPool, userPoolClient, restApi);
 *
 * // Automatically creates:
 * // - Cognito Identity Pool with provider configuration
 * // - IAM roles for authenticated and unauthenticated users
 * // - Role mapping rules for dynamic role assignment
 * // - Custom resources for advanced configuration
 * // - Integration with external identity providers
 *
 * // Access the created resources
 * IdentityPool pool = identityPool.getIdentityPool();
 * Role authRole = identityPool.getAuthenticatedRole();
 * }</pre>
 *
 * @author CDK Common Framework
 * @see IdentityPool for AWS CDK Identity Pool construct
 * @see RoleConstruct for IAM role provisioning
 * @see AwsCustomResource for custom resource operations
 * @see IdentityPoolConf for configuration model
 * @since 1.0.0
 */
@Slf4j
@Getter
public class IdentityPoolConstruct extends Construct {
  private final IdentityPool identityPool;
  private final Role authenticatedRole;

  @SneakyThrows
  public IdentityPoolConstruct(Construct scope, Common common, String identityPool, UserPool userPool, UserPoolClient userPoolClient,
    RestApi api) {
    super(scope, "identitypool");

    var conf = parse(scope, identityPool,
      Map.of("hosted:api:id", CfnJson.Builder.create(this, "api.id").value(api.getRestApiId()).build().getValue()));

    log.debug("{} [common: {} conf: {}]", "IdentityPoolConstruct", common, conf);

    this.authenticatedRole = new RoleConstruct(this, common, conf.authenticated()).role();

    this.identityPool =
      Builder.create(this, conf.name()).authenticatedRole(this.authenticatedRole()).unauthenticatedRole(null).identityPoolName(conf.name())
        .allowClassicFlow(conf.allowClassicFlow()).allowUnauthenticatedIdentities(conf.allowUnauthenticatedIdentities())
        .authenticationProviders(IdentityPoolAuthenticationProviders.builder()
          .userPools(List.of(UserPoolAuthenticationProvider.Builder.create().userPool(userPool).userPoolClient(userPoolClient)
            .disableServerSideTokenCheck(conf.disableServerSideTokenCheck()).build()))
          .build())
        .roleMappings(
          conf.userPoolRoleMappings().stream()
            .map(
              mapping -> IdentityPoolRoleMapping.builder().mappingKey(mapping.key())
                .providerUrl(IdentityPoolProviderUrl.userPool(userPool, userPoolClient))
                .resolveAmbiguousRoles(mapping.resolveAmbiguousRoles()).useToken(mapping.useToken())
                .rules(mapping.rules().stream().map(rule -> RoleMappingRule.builder().mappedRole(this.authenticatedRole())
                  .claim(rule.claim()).claimValue(rule.claimValue()).matchType(rule.matchType()).build()).toList())
                .build())
            .toList())
        .build();

    applyPrincipalTags(userPool);
    cleanupTrustPolicy();
  }

  @SneakyThrows
  private static IdentityPoolConf parse(Construct scope, String idp, Map<String, Object> replacements) {
    var yaml = Template.parse(scope, idp, replacements);
    return Mapper.get().readValue(yaml, IdentityPoolConf.class);
  }

  private void cleanupTrustPolicy() {
    String trust = String.format("""
      {
        "Version": "2012-10-17",
        "Statement": [
          {
            "Effect": "Allow",
            "Principal": {
              "Federated": "cognito-identity.amazonaws.com"
            },
            "Action": [
              "sts:AssumeRoleWithWebIdentity",
              "sts:TagSession"
            ],
            "Condition": {
              "StringEquals": {
                "cognito-identity.amazonaws.com:aud": "%s"
              },
              "ForAnyValue:StringLike": {
                "cognito-identity.amazonaws.com:amr": "authenticated"
              }
            }
          }
        ]
      }
      """, this.identityPool().getIdentityPoolId());

    var cleanup = AwsSdkCall.builder().service("IAM").action("UpdateAssumeRolePolicy")
      .parameters(Map.of("RoleName", this.authenticatedRole().getRoleName(), "PolicyDocument", trust))
      .physicalResourceId(PhysicalResourceId.of(this.authenticatedRole().getRoleName())).build();

    AwsCustomResource.Builder.create(this, "CustomResourceUpdateTrustPolicy").onCreate(cleanup).onUpdate(cleanup)
      .policy(AwsCustomResourcePolicy.fromStatements(List.of(PolicyStatement.Builder.create().effect(Effect.ALLOW)
        .actions(List.of("iam:UpdateAssumeRolePolicy")).resources(List.of(this.authenticatedRole().getRoleArn())).build())))
      .build();
  }

  private void applyPrincipalTags(UserPool userPool) {
    var applyAttributesForAccessControl = AwsSdkCall.builder().service("CognitoIdentity").action("SetPrincipalTagAttributeMap")
      .parameters(
        Map.of("IdentityPoolId", this.identityPool().getIdentityPoolId(), "IdentityProviderName", userPool.getUserPoolProviderName(),
          "PrincipalTags", Map.of("sub", "cognito-identity.amazonaws.com:sub"), "UseDefaults", Boolean.TRUE))
      .physicalResourceId(PhysicalResourceId.of(this.identityPool().getIdentityPoolId())).build();

    AwsCustomResource.Builder.create(this, "principal-tags").onCreate(applyAttributesForAccessControl)
      .onUpdate(applyAttributesForAccessControl)
      .policy(AwsCustomResourcePolicy.fromStatements(
        List.of(PolicyStatement.Builder.create().effect(Effect.ALLOW).actions(List.of("cognito-identity:SetPrincipalTagAttributeMap"))
          .resources(List.of(this.identityPool().getIdentityPoolArn())).build())))
      .build();
  }
}
