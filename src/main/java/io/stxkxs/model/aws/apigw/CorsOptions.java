package io.stxkxs.model.aws.apigw;

import java.util.List;

public record CorsOptions(
  List<String> allowOrigins,
  List<String> allowHeaders,
  List<String> allowMethods
) {}
