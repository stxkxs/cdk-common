package io.stxkxs.execute.aws.apigw;

import io.stxkxs.execute.aws.lambda.LambdaConstruct;
import io.stxkxs.execute.serialization.Mapper;
import io.stxkxs.execute.serialization.Template;
import io.stxkxs.model._main.Common;
import io.stxkxs.model.aws.fn.ApiGatewayLambda;
import io.stxkxs.model.aws.fn.Integration;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.services.apigateway.*;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.lambda.LayerVersion;
import software.constructs.Construct;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import static java.util.stream.Collectors.toMap;

@Slf4j
public class RestApiIntegration {

  public static IResource get(
    Construct scope,
    Common common,
    String ref,
    Vpc vpc,
    RestApiConstruct stack,
    IResource parent,
    Map<String, Map<String, Model>> requestModels,
    LayerVersion... layers
  ) {
    var conf = parse(scope, ref);
    var fn = new LambdaConstruct(scope, common, conf.fn(), vpc, layers).function();

    log.debug("rest api integration configuration with indeterminate layers [common: {} integration: {}]", common, conf);

    return integrate(scope, null, stack, parent, requestModels, conf, fn);
  }

  public static IResource get(
    Construct scope,
    Common common,
    Authorizer authorizer,
    String ref,
    Vpc vpc,
    RestApiConstruct stack,
    IResource parent,
    Map<String, Map<String, Model>> requestModels,
    LayerVersion baseLayer
  ) {
    var conf = parse(scope, ref);
    var fn = new LambdaConstruct(scope, common, conf.fn(), vpc, baseLayer).function();

    log.debug("rest api integration configuration with base layer [common: {} integration: {}]", common, conf);

    return integrate(scope, authorizer, stack, parent, requestModels, conf, fn);
  }

  public static IResource get(
    Construct scope,
    Common common,
    Authorizer authorizer,
    String ref,
    Vpc vpc,
    RestApiConstruct stack,
    IResource parent,
    Map<String, Map<String, Model>> requestModels
  ) {
    var conf = parse(scope, ref);
    var fn = new LambdaConstruct(scope, common, conf.fn(), vpc).function();

    log.debug("rest api integration configuration [common: {} integration: {}]", common, conf);

    return integrate(scope, authorizer, stack, parent, requestModels, conf, fn);
  }

  private static IResource integrate(Construct scope, Authorizer authorizer, RestApiConstruct stack, IResource parent, Map<String, Map<String, Model>> requestModels, ApiGatewayLambda conf, software.amazon.awscdk.services.lambda.Function fn) {
    conf.integration().forEach(i -> {
      var integration = LambdaIntegration.Builder
        .create(fn)
        .allowTestInvoke(i.allowTestInvoke())
        .cacheNamespace(i.cacheNamespace())
        .cacheKeyParameters(i.cacheKeyParameters())
        .connectionType(i.connectionType())
        .passthroughBehavior(i.passthroughBehavior())
        .contentHandling(i.contentHandling())
        .proxy(i.proxy())
        .build();

      parent.resourceForPath(i.path())
        .addMethod(i.method().name(), integration, methodOptions(scope, authorizer, stack, requestModels, i));
    });

    return parent;
  }

  private static @NotNull MethodOptions methodOptions(Construct scope, Authorizer authorizer, RestApiConstruct stack, Map<String, Map<String, Model>> requestModels, Integration i) {
    var options = MethodOptions.builder()
      .operationName(i.options().operationName())
      .authorizationType(i.options().authorizationType())
      .apiKeyRequired(i.options().apiKeyRequired())
      .requestValidator(stack.validators().get(i.options().requestValidator()))
      .methodResponses(i.options()
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
                  .create(scope, model.getValue().modelName())
                  .modelName(model.getValue().modelName())
                  .description(model.getValue().description())
                  .contentType(model.getValue().contentType())
                  .restApi(stack.api())
                  .build()))
              .collect(toMap(Entry::getKey, Entry::getValue)))
            .build())
        .toList())
      .requestParameters(i.options().requestParameters());

    if (authorizer != null && i.options().authorizationType().equals(AuthorizationType.COGNITO)) {
      options.authorizer(authorizer);
      options.authorizationScopes(i.options().authorizationScopes());
    }

    if (!i.options().requestModels().isEmpty())
      options.requestModels(i.options().requestModels().stream()
        .map(requestModels::get)
        .findFirst().get());

    return options.build();
  }

  @SneakyThrows
  private static ApiGatewayLambda parse(Construct scope, String conf) {
    var yaml = Template.parse(scope, conf);
    return Mapper.get().readValue(yaml, ApiGatewayLambda.class);
  }
}
