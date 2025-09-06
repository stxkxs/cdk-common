package io.stxkxs.model.aws.s3;

public record BucketLifecycleRule(boolean enabled, int expiration, String id) {}
