package io.stxkxs.model.aws.cognito.userpool;

import io.stxkxs.model.aws.iam.IamRole;

public record Sns(boolean enabled, String externalId, IamRole role) {}
