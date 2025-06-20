package io.stxkxs.model.aws.fn;

import software.amazon.awscdk.services.apigateway.ConnectionType;
import software.amazon.awscdk.services.apigateway.ContentHandling;
import software.amazon.awscdk.services.apigateway.PassthroughBehavior;
import software.amazon.awscdk.services.lambda.HttpMethod;

import java.util.List;

public record Integration(
  String path,
  HttpMethod method,
  boolean allowTestInvoke,
  String cacheNamespace,
  List<String> cacheKeyParameters,
  ContentHandling contentHandling,
  ConnectionType connectionType,
  PassthroughBehavior passthroughBehavior,
  boolean proxy,
  IntegrationOptions options
) {}
