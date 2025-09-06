package io.stxkxs.model.aws.eks;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.stxkxs.model.aws.iam.IamRole;
import java.util.Map;

public record PodIdentity(ObjectMeta metadata, IamRole role, Map<String, String> tags) {}
