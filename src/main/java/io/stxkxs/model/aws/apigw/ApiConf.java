package io.stxkxs.model.aws.apigw;

import io.stxkxs.model.aws.apigw.usageplan.UsagePlanConf;
import io.stxkxs.model.aws.cloudwatch.LogGroupConf;
import io.stxkxs.model.aws.fn.LambdaLayer;
import io.stxkxs.model.aws.fn.RequestModel;
import io.stxkxs.model.aws.fn.RequestValidator;
import software.amazon.awscdk.services.apigateway.AuthorizationType;

import java.util.List;
import java.util.Map;

public record ApiConf(
  String vpcName,
  String name,
  String description,
  LambdaLayer baseLayer,
  String architecture,
  boolean cloudwatchEnabled,
  boolean disableExecuteApi,
  boolean apiKeyRequired,
  AuthorizationType authorizationType,
  List<UsagePlanConf> usagePlans,
  List<RequestValidator> validators,
  List<RequestModel> requestModels,
  List<MethodResponse> methodResponses,
  StageOptions stageOptions,
  LogGroupConf logGroup,
  Map<String, String> tags
) {}
