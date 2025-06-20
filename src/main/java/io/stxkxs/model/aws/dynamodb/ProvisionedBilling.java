package io.stxkxs.model.aws.dynamodb;

public record ProvisionedBilling(
  Provisioned read,
  Provisioned write
) {}
