package io.stxkxs.model.aws.eks;

import java.util.List;

public record TenancyConf(List<Tenant> administrators, List<Tenant> users) {}
