package io.stxkxs.model.aws.s3;

import io.stxkxs.model.aws.iam.Principal;

import java.util.List;
import java.util.Map;

public record BucketPolicyConf(
  String name,
  List<Principal> principals,
  String policy,
  Map<String, Object> mappings
) {}
