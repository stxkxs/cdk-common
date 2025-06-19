package io.stxkxs.model.aws.eks;

import java.util.List;
import java.util.Map;

public record KubernetesConf(
  String name,
  String version,
  String endpointAccess,
  boolean prune,
  String rbac,
  String tenancy,
  List<String> loggingTypes,
  List<String> vpcSubnetTypes,
  String nodeGroups,
  String addons,
  String sqs,
  String observability,
  Map<String, String> annotations,
  Map<String, String> labels,
  Map<String, String> tags
) {}