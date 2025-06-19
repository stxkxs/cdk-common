package io.stxkxs.execute.aws.cognito;

import io.stxkxs.execute.aws.iam.RoleConstruct;
import io.stxkxs.execute.serialization.Mapper;
import io.stxkxs.execute.serialization.Template;
import io.stxkxs.model._main.Common;
import io.stxkxs.model.aws.cognito.identitypool.IdentityPoolConf;
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

import java.util.List;
import java.util.Map;

@Slf4j
@Getter
public class IdentityPoolConstruct extends Construct {
  private final IdentityPool identityPool;
  private final Role authenticatedRole;

  @SneakyThrows
  public IdentityPoolConstruct(Construct scope, Common common, String identityPool, UserPool userPool, UserPoolClient userPoolClient, RestApi api) {
    super(scope, "identitypool");

    var conf = parse(scope, identityPool, Map.of("hosted:api:id",
      CfnJson.Builder.create(this, "api.id").value(api.getRestApiId()).build().getValue()));

    log.debug("identity pool configuration [common: {} identity-pool: {}]", common, conf);

    this.authenticatedRole = new RoleConstruct(this, common, conf.authenticated()).role();

    this.identityPool = Builder
      .create(this, conf.name())
      .authenticatedRole(this.authenticatedRole())
      .unauthenticatedRole(null)
      .identityPoolName(conf.name())
      .allowClassicFlow(conf.allowClassicFlow())
      .allowUnauthenticatedIdentities(conf.allowUnauthenticatedIdentities())
      .authenticationProviders(
        IdentityPoolAuthenticationProviders.builder()
          .userPools(List.of(
            UserPoolAuthenticationProvider.Builder.create()
              .userPool(userPool)
              .userPoolClient(userPoolClient)
              .disableServerSideTokenCheck(conf.disableServerSideTokenCheck())
              .build()))
          .build())
      .roleMappings(conf.userPoolRoleMappings().stream()
        .map(mapping ->
          IdentityPoolRoleMapping.builder()
            .mappingKey(mapping.key())
            .providerUrl(IdentityPoolProviderUrl.userPool(userPool, userPoolClient))
            .resolveAmbiguousRoles(mapping.resolveAmbiguousRoles())
            .useToken(mapping.useToken())
            .rules(mapping.rules().stream()
              .map(rule ->
                RoleMappingRule.builder()
                  .mappedRole(this.authenticatedRole())
                  .claim(rule.claim())
                  .claimValue(rule.claimValue())
                  .matchType(rule.matchType())
                  .build())
              .toList())
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

    var cleanup = AwsSdkCall.builder()
      .service("IAM")
      .action("UpdateAssumeRolePolicy")
      .parameters(Map.of(
        "RoleName", this.authenticatedRole().getRoleName(),
        "PolicyDocument", trust))
      .physicalResourceId(PhysicalResourceId.of(this.authenticatedRole().getRoleName()))
      .build();

    AwsCustomResource.Builder.create(this, "CustomResourceUpdateTrustPolicy")
      .onCreate(cleanup)
      .onUpdate(cleanup)
      .policy(AwsCustomResourcePolicy.fromStatements(List.of(
        PolicyStatement.Builder.create()
          .effect(Effect.ALLOW)
          .actions(List.of("iam:UpdateAssumeRolePolicy"))
          .resources(List.of(this.authenticatedRole().getRoleArn()))
          .build())))
      .build();
  }

  private void applyPrincipalTags(UserPool userPool) {
    var applyAttributesForAccessControl = AwsSdkCall.builder()
      .service("CognitoIdentity")
      .action("SetPrincipalTagAttributeMap")
      .parameters(Map.of(
        "IdentityPoolId", this.identityPool().getIdentityPoolId(),
        "IdentityProviderName", userPool.getUserPoolProviderName(),
        "PrincipalTags", Map.of("sub", "cognito-identity.amazonaws.com:sub"),
        "UseDefaults", Boolean.TRUE))
      .physicalResourceId(PhysicalResourceId.of(this.identityPool().getIdentityPoolId()))
      .build();

    AwsCustomResource.Builder
      .create(this, "principal-tags")
      .onCreate(applyAttributesForAccessControl)
      .onUpdate(applyAttributesForAccessControl)
      .policy(AwsCustomResourcePolicy.fromStatements(List.of(
        PolicyStatement.Builder.create()
          .effect(Effect.ALLOW)
          .actions(List.of("cognito-identity:SetPrincipalTagAttributeMap"))
          .resources(List.of(this.identityPool().getIdentityPoolArn()))
          .build())))
      .build();
  }
}
