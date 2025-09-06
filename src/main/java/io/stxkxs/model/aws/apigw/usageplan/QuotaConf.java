package io.stxkxs.model.aws.apigw.usageplan;

import software.amazon.awscdk.services.apigateway.Period;

public record QuotaConf(boolean enabled, Integer limit, Period period) {}
