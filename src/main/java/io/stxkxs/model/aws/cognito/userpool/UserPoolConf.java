package io.stxkxs.model.aws.cognito.userpool;

import java.util.List;
import java.util.Map;

public record UserPoolConf(
  String name,
  String triggers,
  String accountRecovery,
  String standardThreatProtectionMode,
  String sns,
  String ses,
  List<Group> groups,
  AutoVerify autoVerify,
  Mfa mfa,
  PasswordPolicy passwordPolicy,
  UserVerification verification,
  DeviceTracking deviceTracking,
  SignInAliases aliases,
  Map<StandardAttributeKey, UserAttribute> standardAttributes,
  List<CustomAttribute> customAttributes,
  KeepOriginalAttributes keepOriginalAttributes,
  boolean signInCaseSensitive,
  boolean deletionProtection,
  boolean selfSignup,
  String removalPolicy,
  Map<String, String> tags
) {}
