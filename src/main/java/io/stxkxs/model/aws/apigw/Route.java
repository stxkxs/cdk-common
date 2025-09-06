package io.stxkxs.model.aws.apigw;

import java.util.List;

public record Route(List<String> methods, String path) {}
