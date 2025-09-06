package io.stxkxs.model.aws.fn;

import java.util.List;
import software.amazon.awscdk.services.apigateway.ConnectionType;
import software.amazon.awscdk.services.apigateway.ContentHandling;
import software.amazon.awscdk.services.apigateway.PassthroughBehavior;
import software.amazon.awscdk.services.lambda.HttpMethod;

public record Integration(String path, HttpMethod method, boolean allowTestInvoke, String cacheNamespace, List<String> cacheKeyParameters,
  ContentHandling contentHandling, ConnectionType connectionType, PassthroughBehavior passthroughBehavior, boolean proxy,
  IntegrationOptions options) {}
