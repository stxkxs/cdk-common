package io.stxkxs.model.aws.apigw.usageplan;

public record UsagePlanConf(String name, String description, ThrottleConf throttle, QuotaConf quota) {}
