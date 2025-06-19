package io.stxkxs.model.aws.codebuild;

import io.stxkxs.model.aws.s3.S3Bucket;

public record Certificate(
  S3Bucket bucket,
  String objectKey
) {}
