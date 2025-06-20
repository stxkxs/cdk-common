package io.stxkxs.execute.aws.cognito;

import io.stxkxs.execute.serialization.Mapper;
import io.stxkxs.execute.serialization.Template;
import io.stxkxs.model._main.Common;
import io.stxkxs.model._main.Common.Maps;
import io.stxkxs.model.aws.cognito.userpool.CustomAttribute;
import io.stxkxs.model.aws.cognito.userpool.CustomAttributeType;
import io.stxkxs.model.aws.cognito.userpool.StandardAttributeKey;
import io.stxkxs.model.aws.cognito.userpool.UserAttribute;
import io.stxkxs.model.aws.cognito.userpool.UserPoolConf;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Tags;
import software.amazon.awscdk.services.cognito.AccountRecovery;
import software.amazon.awscdk.services.cognito.AutoVerifiedAttrs;
import software.amazon.awscdk.services.cognito.CfnUserPoolGroup;
import software.amazon.awscdk.services.cognito.DateTimeAttribute;
import software.amazon.awscdk.services.cognito.DeviceTracking;
import software.amazon.awscdk.services.cognito.ICustomAttribute;
import software.amazon.awscdk.services.cognito.KeepOriginalAttrs;
import software.amazon.awscdk.services.cognito.Mfa;
import software.amazon.awscdk.services.cognito.MfaSecondFactor;
import software.amazon.awscdk.services.cognito.NumberAttribute;
import software.amazon.awscdk.services.cognito.PasswordPolicy;
import software.amazon.awscdk.services.cognito.SignInAliases;
import software.amazon.awscdk.services.cognito.StandardAttribute;
import software.amazon.awscdk.services.cognito.StandardAttributes;
import software.amazon.awscdk.services.cognito.StandardThreatProtectionMode;
import software.amazon.awscdk.services.cognito.StringAttribute;
import software.amazon.awscdk.services.cognito.UserPool;
import software.amazon.awscdk.services.cognito.UserPool.Builder;
import software.amazon.awscdk.services.cognito.UserVerificationConfig;
import software.amazon.awscdk.services.cognito.VerificationEmailStyle;
import software.amazon.awscdk.services.ec2.IVpc;
import software.constructs.Construct;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.stxkxs.execute.serialization.Format.id;

@Slf4j
@Getter
public class UserPoolConstruct extends Construct {
  private final UserPool userPool;
  private final List<CfnUserPoolGroup> groups;

  @SneakyThrows
  public UserPoolConstruct(Construct scope, Common common, String userPool, IVpc vpc) {
    super(scope, id("userpool", common.name()));

    var userPoolYaml = Template.parse(scope, userPool);
    var userPoolConf = Mapper.get().readValue(userPoolYaml, UserPoolConf.class);

    log.debug("userpool configuration [common: {} userpool: {}]", common, userPoolConf);

    var triggers = new UserPoolTriggersConstruct(this, common, vpc, userPoolConf).triggers();
    var ses = new UserPoolSesConstruct(this, common, userPoolConf);
    var sms = new UserPoolSnsConstruct(this, common, userPoolConf);

    this.userPool = Builder
      .create(this, userPoolConf.name())
      .userPoolName(userPoolConf.name())
      .signInCaseSensitive(userPoolConf.signInCaseSensitive())
      .signInAliases(SignInAliases.builder()
        .username(userPoolConf.aliases().username())
        .email(userPoolConf.aliases().email())
        .phone(userPoolConf.aliases().phone())
        .preferredUsername(userPoolConf.aliases().preferredUsername())
        .build())
      .smsRole(sms.role())
      .smsRoleExternalId(sms.externalId())
      .email(ses.email())
      .autoVerify(AutoVerifiedAttrs.builder()
        .email(userPoolConf.autoVerify().email())
        .phone(userPoolConf.autoVerify().phone())
        .build())
      .keepOriginal(KeepOriginalAttrs.builder()
        .email(userPoolConf.keepOriginalAttributes().email())
        .phone(userPoolConf.keepOriginalAttributes().phone())
        .build())
      .lambdaTriggers(triggers)
      .passwordPolicy(PasswordPolicy.builder()
        .minLength(userPoolConf.passwordPolicy().minLength())
        .requireSymbols(userPoolConf.passwordPolicy().requireSymbols())
        .requireLowercase(userPoolConf.passwordPolicy().requireLowercase())
        .requireDigits(userPoolConf.passwordPolicy().requireDigits())
        .tempPasswordValidity(Duration.days(userPoolConf.passwordPolicy().tempPasswordValidity()))
        .build())
      .userVerification(UserVerificationConfig.builder()
        .emailStyle(VerificationEmailStyle.valueOf(userPoolConf.verification().emailStyle().toUpperCase()))
        .emailBody(userPoolConf.verification().emailBody())
        .emailSubject(userPoolConf.verification().emailSubject())
        .smsMessage(userPoolConf.verification().smsMessage())
        .build())
      .deviceTracking(DeviceTracking.builder()
        .challengeRequiredOnNewDevice(userPoolConf.deviceTracking().newDeviceChallenge())
        .deviceOnlyRememberedOnUserPrompt(userPoolConf.deviceTracking().rememberOnUserPrompt())
        .build())
      .mfa(Mfa.valueOf(userPoolConf.mfa().type().toUpperCase()))
      .mfaMessage(userPoolConf.mfa().message())
      .mfaSecondFactor(MfaSecondFactor.builder()
        .sms(userPoolConf.mfa().sms())
        .otp(userPoolConf.mfa().otp())
        .build())
      .selfSignUpEnabled(userPoolConf.selfSignup())
      .standardAttributes(standardAttributes(userPoolConf.standardAttributes()))
      .customAttributes(customAttributes(userPoolConf.customAttributes()))
      .standardThreatProtectionMode(StandardThreatProtectionMode.valueOf(userPoolConf.standardThreatProtectionMode().toUpperCase()))
      .accountRecovery(AccountRecovery.valueOf(userPoolConf.accountRecovery().toUpperCase()))
      .removalPolicy(RemovalPolicy.valueOf(userPoolConf.removalPolicy().toUpperCase()))
      .deletionProtection(userPoolConf.deletionProtection())
      .build();

    this.groups = userPoolConf.groups()
      .stream()
      .map(group ->
        CfnUserPoolGroup.Builder
          .create(this, group.name())
          .groupName(group.name())
          .userPoolId(this.userPool().getUserPoolId())
          .description(group.description())
          .precedence(group.precedence())
          .build())
      .toList();

    Maps.from(common.tags(), userPoolConf.tags())
      .forEach((key, value) -> Tags.of(this.userPool()).add(key, value));
  }

  private Map<String, ? extends ICustomAttribute> customAttributes(List<CustomAttribute> customAttributes) {
    Function<CustomAttribute, ICustomAttribute> schema = (CustomAttribute attribute) ->
      switch (CustomAttributeType.valueOf(attribute.type().toUpperCase())) {
        case CustomAttributeType.DATE -> DateTimeAttribute.Builder.create()
          .mutable(attribute.mutable())
          .build();
        case CustomAttributeType.STRING -> StringAttribute.Builder.create()
          .minLen(attribute.min())
          .maxLen(attribute.max())
          .mutable(attribute.mutable())
          .build();
        case CustomAttributeType.NUMBER -> NumberAttribute.Builder.create()
          .min(attribute.min())
          .max(attribute.max())
          .mutable(attribute.mutable())
          .build();
      };

    return customAttributes.stream()
      .map(attribute -> Map.entry(attribute.name(), schema.apply(attribute)))
      .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
  }

  private StandardAttributes standardAttributes(Map<StandardAttributeKey, UserAttribute> standardAttributes) {
    var attributes = StandardAttributes.builder();
    standardAttributes.forEach((key, value) -> {
      switch (key) {
        case StandardAttributeKey.address -> attributes.address(
          StandardAttribute.builder()
            .required(value.required())
            .mutable(value.mutable())
            .build());
        case StandardAttributeKey.birthdate -> attributes.birthdate(
          StandardAttribute.builder()
            .required(value.required())
            .mutable(value.mutable())
            .build());
        case StandardAttributeKey.email -> attributes.email(
          StandardAttribute.builder()
            .required(value.required())
            .mutable(value.mutable())
            .build());
        case StandardAttributeKey.familyName -> attributes.familyName(
          StandardAttribute.builder()
            .required(value.required())
            .mutable(value.mutable())
            .build());
        case StandardAttributeKey.fullname -> attributes.fullname(
          StandardAttribute.builder()
            .required(value.required())
            .mutable(value.mutable())
            .build());
        case StandardAttributeKey.gender -> attributes.gender(
          StandardAttribute.builder()
            .required(value.required())
            .mutable(value.mutable())
            .build());
        case StandardAttributeKey.givenName -> attributes.givenName(
          StandardAttribute.builder()
            .required(value.required())
            .mutable(value.mutable())
            .build());
        case StandardAttributeKey.lastUpdateTime -> attributes.lastUpdateTime(
          StandardAttribute.builder()
            .required(value.required())
            .mutable(value.mutable())
            .build());
        case StandardAttributeKey.locale -> attributes.locale(
          StandardAttribute.builder()
            .required(value.required())
            .mutable(value.mutable())
            .build());
        case StandardAttributeKey.middleName -> attributes.middleName(
          StandardAttribute.builder()
            .required(value.required())
            .mutable(value.mutable())
            .build());
        case StandardAttributeKey.nickname -> attributes.nickname(
          StandardAttribute.builder()
            .required(value.required())
            .mutable(value.mutable())
            .build());
        case StandardAttributeKey.phoneNumber -> attributes.phoneNumber(
          StandardAttribute.builder()
            .required(value.required())
            .mutable(value.mutable())
            .build());
        case StandardAttributeKey.preferredUsername -> attributes.preferredUsername(
          StandardAttribute.builder()
            .required(value.required())
            .mutable(value.mutable())
            .build());
        case StandardAttributeKey.profilePage -> attributes.profilePage(
          StandardAttribute.builder()
            .required(value.required())
            .mutable(value.mutable())
            .build());
        case StandardAttributeKey.profilePicture -> attributes.profilePicture(
          StandardAttribute.builder()
            .required(value.required())
            .mutable(value.mutable())
            .build());
        case StandardAttributeKey.timezone -> attributes.timezone(
          StandardAttribute.builder()
            .required(value.required())
            .mutable(value.mutable())
            .build());
        case StandardAttributeKey.website -> attributes.website(
          StandardAttribute.builder()
            .required(value.required())
            .mutable(value.mutable())
            .build());
      }
    });

    return attributes.build();
  }
}
