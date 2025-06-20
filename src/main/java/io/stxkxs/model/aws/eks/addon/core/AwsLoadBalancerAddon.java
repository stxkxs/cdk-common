package io.stxkxs.model.aws.eks.addon.core;

import io.stxkxs.model.aws.eks.HelmChart;
import io.stxkxs.model.aws.eks.ServiceAccountConf;

public record AwsLoadBalancerAddon(
  HelmChart chart,
  ServiceAccountConf serviceAccount
) {}
