package io.stxkxs.model.aws.cognito.client;

import java.util.List;

public record CognitoOAuth(
  boolean authorizationCodeGrant,
  List<String> callbackUrls,
  boolean clientCredentials,
  boolean implicitCodeGrant,
  List<String> logoutUrls,
  List<String> scopes
) {}
