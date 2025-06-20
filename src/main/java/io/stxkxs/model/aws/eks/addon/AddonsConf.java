package io.stxkxs.model.aws.eks.addon;

import io.stxkxs.model.aws.eks.addon.core.AwsLoadBalancerAddon;
import io.stxkxs.model.aws.eks.addon.core.CertManagerAddon;
import io.stxkxs.model.aws.eks.addon.core.GrafanaAddon;
import io.stxkxs.model.aws.eks.addon.core.karpenter.KarpenterAddon;
import io.stxkxs.model.aws.eks.addon.core.secretprovider.AwsSecretsStoreAddon;
import io.stxkxs.model.aws.eks.addon.core.secretprovider.CsiSecretsStoreAddon;
import io.stxkxs.model.aws.eks.addon.managed.ManagedAddons;

public record AddonsConf(
  ManagedAddons managed,
  CsiSecretsStoreAddon csiSecretsStore,
  AwsSecretsStoreAddon awsSecretsStore,
  AwsLoadBalancerAddon awsLoadBalancer,
  CertManagerAddon certManager,
  KarpenterAddon karpenter,
  GrafanaAddon grafana
) {}
