package io.stxkxs.model.aws.eks.addon.core.karpenter;

import io.stxkxs.model.aws.eks.HelmChart;
import io.stxkxs.model.aws.eks.PodIdentity;

public record KarpenterAddon(
  HelmChart chart,
  PodIdentity podIdentity
) {}
