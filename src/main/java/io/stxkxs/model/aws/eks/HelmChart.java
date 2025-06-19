package io.stxkxs.model.aws.eks;

public record HelmChart(
  String name,
  String namespace,
  String release,
  String repository,
  String values,
  String version
) {}
