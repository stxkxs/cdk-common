package io.stxkxs.model.aws.eks.addon.managed;

public record ManagedAddons(
  AwsEbsCsiAddon awsEbsCsi,
  ManagedAddon awsVpcCni,
  ManagedAddon coreDns,
  ManagedAddon kubeProxy,
  ManagedAddon containerInsights,
  ManagedAddon podIdentityAgent
) {}
