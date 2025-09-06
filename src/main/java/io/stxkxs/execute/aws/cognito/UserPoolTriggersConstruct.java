package io.stxkxs.execute.aws.cognito;

import static io.stxkxs.execute.serialization.Format.id;

import io.stxkxs.execute.aws.lambda.LambdaConstruct;
import io.stxkxs.execute.serialization.Mapper;
import io.stxkxs.execute.serialization.Template;
import io.stxkxs.model._main.Common;
import io.stxkxs.model.aws.cognito.userpool.Triggers;
import io.stxkxs.model.aws.cognito.userpool.UserPoolConf;
import io.stxkxs.model.aws.fn.Lambda;
import java.util.List;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import software.amazon.awscdk.services.cognito.UserPoolTriggers;
import software.amazon.awscdk.services.ec2.IVpc;
import software.amazon.awscdk.services.lambda.Architecture;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.IFunction;
import software.amazon.awscdk.services.lambda.LayerVersion;
import software.amazon.awscdk.services.lambda.Runtime;
import software.constructs.Construct;

@Slf4j
@Getter
public class UserPoolTriggersConstruct {
  private final UserPoolTriggers triggers;

  @SneakyThrows
  public UserPoolTriggersConstruct(Construct scope, Common common, IVpc vpc, UserPoolConf conf) {
    log.debug("{} [common: {} conf: {}]", "UserPoolTriggersConstruct", common, conf);

    var triggersYaml = Template.parse(scope, conf.triggers());
    var triggers = Mapper.get().readValue(triggersYaml, Triggers.class);

    var baseLayer = LayerVersion.Builder.create(scope, id("layer", triggers.base().name())).layerVersionName(triggers.base().name())
      .code(Code.fromAsset(triggers.base().asset())).removalPolicy(triggers.base().removalPolicy())
      .compatibleArchitectures(List.of(Architecture.X86_64))
      .compatibleRuntimes(triggers.base().runtimes().stream().map(r -> Runtime.Builder.create(r).build()).toList()).build();

    this.triggers = UserPoolTriggers.builder().customMessage(maybe(scope, common, vpc, triggers.customMessage(), baseLayer))
      .preSignUp(maybe(scope, common, vpc, triggers.preSignUp(), baseLayer))
      .postConfirmation(maybe(scope, common, vpc, triggers.postConfirmation(), baseLayer))
      .postAuthentication(maybe(scope, common, vpc, triggers.postAuthentication(), baseLayer)).build();
  }

  private static @Nullable IFunction maybe(Construct scope, Common common, IVpc vpc, Lambda lambda, LayerVersion baseLayer) {
    return lambda != null ? new LambdaConstruct(scope, common, lambda, vpc, baseLayer).function() : null;
  }
}
