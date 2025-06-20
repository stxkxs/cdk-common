package io.stxkxs.model.aws.bcm;

public record DestinationConfigurations(
  String region,
  String bucket,
  String prefix,
  OutputConfiguration outputConfigurations
) {}
