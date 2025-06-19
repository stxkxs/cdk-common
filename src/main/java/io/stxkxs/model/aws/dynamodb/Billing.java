package io.stxkxs.model.aws.dynamodb;

public record Billing(
  boolean onDemand,
  FixedBilling fixed,
  ProvisionedBilling provisioned
) {}
