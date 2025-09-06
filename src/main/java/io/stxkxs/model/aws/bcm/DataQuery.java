package io.stxkxs.model.aws.bcm;

import java.util.Map;

public record DataQuery(String queryStatement, Map<String, Map<String, String>> tableConfigurations) {}
