package io.stxkxs.model.aws.fn;

public record RequestValidator(
  String name,
  boolean validateRequestParameters,
  boolean validateRequestBody
) {}
