package io.stxkxs.model.aws.fn;

import io.stxkxs.model.aws.iam.IamRole;
import io.stxkxs.model.aws.iam.Principal;
import software.amazon.awscdk.services.lambda.Runtime;

import java.util.List;
import java.util.Map;

public record Lambda(
  String name,
  String description,
  String asset,
  String handler,
  String subnetType,
  int timeout,
  int memorySize,
  Runtime runtime,
  IamRole role,
  List<Principal> invokers,
  List<LambdaLayer> layers,
  Map<String, String> environment
) {}
