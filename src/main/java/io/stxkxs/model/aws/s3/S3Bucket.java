package io.stxkxs.model.aws.s3;

import io.stxkxs.model.aws.iam.Principal;
import io.stxkxs.model.aws.kms.Kms;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.services.s3.BucketAccessControl;
import software.amazon.awscdk.services.s3.ObjectOwnership;

import java.util.List;
import java.util.Map;

public record S3Bucket(
  String name,
  Principal principal,
  BucketAccessControl accessControl,
  ObjectOwnership objectOwnership,
  List<BucketLifecycleRule> lifecycleRules,
  List<BucketPolicyConf> bucketPolicies,
  boolean eventBridgeEnabled,
  boolean autoDeleteObjects,
  boolean versioned,
  RemovalPolicy removalPolicy,
  Kms kms,
  Map<String, String> tags
) {}
