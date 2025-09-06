package io.stxkxs.model.aws.cognito.identitypool;

import software.amazon.awscdk.services.cognito.identitypool.alpha.RoleMappingMatchType;

public record Rule(String claim, String claimValue, RoleMappingMatchType matchType) {}
