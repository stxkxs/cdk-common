package io.stxkxs.model.aws.msk;

import io.stxkxs.model.aws.eks.ServiceAccountConf;

public record Client(String name, ServiceAccountConf serviceAccount) {}
