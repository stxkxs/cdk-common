package io.stxkxs.model.aws.eks.addon.core;

import io.stxkxs.model.aws.eks.HelmChart;

public record CertManagerAddon(
  HelmChart chart
) {}
