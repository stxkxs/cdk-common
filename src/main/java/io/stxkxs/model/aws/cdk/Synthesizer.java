package io.stxkxs.model.aws.cdk;

/**
 * CDK synthesizer configuration.
 */
public record Synthesizer(String cloudFormationExecutionRole, String deployRoleArn, String fileAssetPublishingRoleArn,
  String imageAssetPublishingRoleArn, String lookupRoleArn, String qualifier, String bucketPrefix, String deployRoleExternalId,
  String dockerTagPrefix, String fileAssetPublishingExternalId, String fileAssetsBucketName, String imageAssetPublishingExternalId,
  String imageAssetsRepositoryName, String lookupRoleExternalId, String bootstrapStackVersionSsmParameter,
  boolean generateBootstrapVersionRule, boolean useLookupRoleForStackOperations) {}
