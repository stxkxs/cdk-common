package io.stxkxs.model.aws.fn;

import software.amazon.awscdk.RemovalPolicy;

import java.util.List;

public record LambdaLayer(
  String name,
  String asset,
  RemovalPolicy removalPolicy,
  List<String> runtimes
) {}
