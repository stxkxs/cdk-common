package io.stxkxs.model.aws.ses.action;

import io.stxkxs.model.aws.fn.Lambda;

public record LambdaActionConf(String topic, Lambda function, String invocationType) {}
