package io.stxkxs.model.aws.eks;

public record Tenant(
  String email,
  String role,
  String username
) {}
