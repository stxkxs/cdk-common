package io.stxkxs.model.aws.apigw;

import java.util.Map;

public record MethodResponse(String statusCode, Map<String, ResponseModel> responseModels, Map<String, Boolean> responseParameters) {}
