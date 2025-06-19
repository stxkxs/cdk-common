package io.stxkxs.model.aws.eks;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.stxkxs.model.aws.iam.IamRole;

public record ServiceAccountConf(
  ObjectMeta metadata,
  IamRole role
) {}
