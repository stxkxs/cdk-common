package io.stxkxs.model.aws.apigw;

import software.amazon.awscdk.services.apigateway.JsonSchema;

@FunctionalInterface
public interface ApiRequestSchema {
  JsonSchema get(String schema);
}
