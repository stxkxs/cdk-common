package io.stxkxs.model._main;

import io.stxkxs.model.aws.ecr.EcrRepository;
import io.stxkxs.model.aws.iam.IamRole;
import io.stxkxs.model.aws.kms.Kms;
import io.stxkxs.model.aws.s3.S3Bucket;

public record SynthesizerResources(
  Kms kms,
  S3Bucket assets,
  EcrRepository ecr,
  IamRole cdkExec,
  IamRole cdkDeploy,
  IamRole cdkLookup,
  IamRole cdkAssets,
  IamRole cdkImages
) {}
