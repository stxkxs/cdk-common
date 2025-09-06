package io.stxkxs.model.aws.sqs;

import java.util.List;

public record SqsEventPattern(List<String> source, List<String> detailType) {}
