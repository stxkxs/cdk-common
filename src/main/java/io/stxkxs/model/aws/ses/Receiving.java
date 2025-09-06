package io.stxkxs.model.aws.ses;

import io.stxkxs.model.aws.s3.S3Bucket;
import java.util.List;

public record Receiving(String name, boolean dropSpam, S3Bucket bucket, List<Rule> rules) {}
