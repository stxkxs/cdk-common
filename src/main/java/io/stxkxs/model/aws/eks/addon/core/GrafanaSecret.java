package io.stxkxs.model.aws.eks.addon.core;

public record GrafanaSecret(
  String key,
  String lokiHost,
  String lokiUsername,
  String prometheusHost,
  String prometheusUsername,
  String tempoHost,
  String tempoUsername
) {}
