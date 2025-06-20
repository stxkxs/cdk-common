package io.stxkxs.model.aws.fn;

import java.util.List;

public record ApiGatewayLambda(
  List<String> paths,
  Lambda fn,
  List<Integration> integration
) {}
