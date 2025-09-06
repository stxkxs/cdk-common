package io.stxkxs.model.aws.fn;

import java.util.List;
import software.amazon.awscdk.RemovalPolicy;

public record LambdaLayer(String name, String asset, RemovalPolicy removalPolicy, List<String> runtimes) {}
