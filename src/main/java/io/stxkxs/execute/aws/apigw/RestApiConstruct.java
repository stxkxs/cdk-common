package io.stxkxs.execute.aws.apigw;

import io.stxkxs.execute.aws.cloudwatch.LogGroupConstruct;
import io.stxkxs.model._main.Common;
import io.stxkxs.model.aws.apigw.ApiConf;
import io.stxkxs.model.aws.apigw.ApiRequestSchema;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.services.apigateway.AccessLogFormat;
import software.amazon.awscdk.services.apigateway.AuthorizationType;
import software.amazon.awscdk.services.apigateway.Authorizer;
import software.amazon.awscdk.services.apigateway.LogGroupLogDestination;
import software.amazon.awscdk.services.apigateway.MethodLoggingLevel;
import software.amazon.awscdk.services.apigateway.MethodOptions;
import software.amazon.awscdk.services.apigateway.MethodResponse;
import software.amazon.awscdk.services.apigateway.Model;
import software.amazon.awscdk.services.apigateway.Model.Builder;
import software.amazon.awscdk.services.apigateway.RequestValidator;
import software.amazon.awscdk.services.apigateway.RestApi;
import software.amazon.awscdk.services.apigateway.StageOptions;
import software.amazon.awscdk.services.logs.ILogGroup;
import software.constructs.Construct;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.stxkxs.execute.serialization.Format.id;
import static java.util.stream.Collectors.toMap;

@Slf4j
@Getter
public class RestApiConstruct extends Construct {
  private final RestApi api;
  private final ILogGroup logGroup;
  private final Map<String, RequestValidator> validators;
  private final Map<String, Map<String, Model>> requestModels;
  private final List<UsagePlanConstruct> usagePlan;

  public RestApiConstruct(Construct scope, Common common, ApiConf conf, ApiRequestSchema schema) {
    super(scope, id("rest.api", common.id(), conf.name()));

    log.debug("rest api configuration [common: {} api: {} schema: {}]", common, conf, schema);

    this.logGroup = new LogGroupConstruct(this, common, conf.logGroup()).logGroup();
    this.api = getRestApi(conf, null);
    this.validators = getValidators(scope, conf);
    this.requestModels = getRequestModels(scope, conf, schema);
    this.usagePlan = usagePlans(common, conf);
  }

  public RestApiConstruct(Construct scope, Common common, ApiConf conf, Authorizer authorizer, ApiRequestSchema schema) {
    super(scope, id("rest.api", common.id(), conf.name()));

    log.debug("rest api configuration with authorizer [common: {} api: {} schema: {}]", common, conf, schema);

    this.logGroup = new LogGroupConstruct(this, common, conf.logGroup()).logGroup();
    this.api = getRestApi(conf, authorizer);
    this.validators = getValidators(scope, conf);
    this.requestModels = getRequestModels(scope, conf, schema);
    this.usagePlan = usagePlans(common, conf);
  }

  private @NotNull List<UsagePlanConstruct> usagePlans(Common common, ApiConf conf) {
    return conf.usagePlans().stream()
      .map(plan -> new UsagePlanConstruct(this, common, plan, this.api()))
      .toList();
  }

  private @NotNull Map<String, Map<String, Model>> getRequestModels(Construct scope, ApiConf conf, ApiRequestSchema schema) {
    return conf.requestModels().stream()
      .map(model -> Map.entry(
        model.name(),
        Map.of(model.contentType(), Builder.create(scope, model.name())
          .modelName(model.name())
          .description(model.description())
          .contentType(model.contentType())
          .schema(schema.get(model.name()))
          .restApi(this.api())
          .build())))
      .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
  }

  private @NotNull Map<String, RequestValidator> getValidators(Construct scope, ApiConf conf) {
    return conf.validators().stream()
      .map(validator -> Map.entry(
        validator.name(), RequestValidator.Builder
          .create(scope, validator.name())
          .requestValidatorName(validator.name())
          .validateRequestParameters(validator.validateRequestParameters())
          .validateRequestBody(validator.validateRequestBody())
          .restApi(this.api())
          .build()))
      .collect(toMap(Entry::getKey, Entry::getValue));
  }

  private @NotNull RestApi getRestApi(ApiConf conf, Authorizer authorizer) {
    return RestApi.Builder
      .create(this, conf.name())
      .restApiName(conf.name())
      .description(conf.description())
      .deployOptions(deployOptions(conf))
      .cloudWatchRole(conf.cloudwatchEnabled())
      .defaultMethodOptions(defaultMethodOptions(conf, authorizer))
      .disableExecuteApiEndpoint(conf.disableExecuteApi())
      .build();
  }

  private MethodOptions defaultMethodOptions(ApiConf conf, Authorizer authorizer) {
    var options = MethodOptions.builder()
      .authorizationType(conf.authorizationType())
      .apiKeyRequired(conf.apiKeyRequired())
      .methodResponses(conf
        .methodResponses()
        .stream()
        .map(methodResponse ->
          MethodResponse.builder()
            .statusCode(methodResponse.statusCode())
            .responseParameters(methodResponse.responseParameters())
            .responseModels(Optional
              .ofNullable(methodResponse.responseModels())
              .orElseGet(Map::of)
              .entrySet()
              .stream()
              .map(model -> Map.entry(
                model.getKey(), Model.Builder
                  .create(this, model.getValue().modelName())
                  .modelName(model.getValue().modelName())
                  .description(model.getValue().description())
                  .contentType(model.getValue().contentType())
                  .build()))
              .collect(toMap(Entry::getKey, Entry::getValue)))
            .build())
        .toList());

    if (authorizer != null && conf.authorizationType().equals(AuthorizationType.COGNITO))
      options.authorizer(authorizer);

    return options.build();
  }

  private StageOptions deployOptions(ApiConf conf) {
    return StageOptions.builder()
      .stageName(conf.stageOptions().stageName())
      .description(conf.stageOptions().description())
      .accessLogFormat(AccessLogFormat.jsonWithStandardFields())
      .accessLogDestination(new LogGroupLogDestination(this.logGroup()))
      .loggingLevel(MethodLoggingLevel.valueOf(conf.stageOptions().loggingLevel().toUpperCase()))
      .tracingEnabled(conf.stageOptions().tracingEnabled())
      .cachingEnabled(conf.stageOptions().cachingEnabled())
      .dataTraceEnabled(conf.stageOptions().dataTraceEnabled())
      .metricsEnabled(conf.stageOptions().metricsEnabled())
      .variables(conf.stageOptions().variables())
      .throttlingBurstLimit(conf.stageOptions().throttlingBurstLimit())
      .throttlingRateLimit(conf.stageOptions().throttlingRateLimit())
      .build();
  }
}
