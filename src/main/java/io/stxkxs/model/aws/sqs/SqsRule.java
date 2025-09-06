package io.stxkxs.model.aws.sqs;

public record SqsRule(String name, String description, boolean enabled, SqsEventPattern eventPattern) {}
