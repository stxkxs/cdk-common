package io.stxkxs.model.aws.cognito.identitypool;

import java.util.List;

public record RoleMappingConf(String key, boolean useToken, boolean resolveAmbiguousRoles, List<Rule> rules) {}
