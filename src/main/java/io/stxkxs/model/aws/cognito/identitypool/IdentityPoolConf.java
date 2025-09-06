package io.stxkxs.model.aws.cognito.identitypool;

import io.stxkxs.model.aws.iam.IamRole;
import java.util.List;

public record IdentityPoolConf(String name, IamRole authenticated, boolean allowClassicFlow, boolean allowUnauthenticatedIdentities,
  boolean disableServerSideTokenCheck, List<RoleMappingConf> userPoolRoleMappings) {}
