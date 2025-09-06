package io.stxkxs.model.aws.cognito.client;

import java.util.List;
import java.util.Map;

public record UserPoolClientConf(String accessTokenValidity, AuthFlow authFlow, String authSessionValidity,
  boolean preventUserExistenceErrors, boolean disableOAuth, boolean enableTokenRevocation, boolean generateSecret,
  boolean enablePropagateAdditionalUserContextData, String idTokenValidity, String name, CognitoOAuth oauth,
  ClientAttributesConf readAttributes, ClientAttributesConf writeAttributes, List<String> customAttributes, String refreshTokenValidity,
  Map<String, String> tags) {}
