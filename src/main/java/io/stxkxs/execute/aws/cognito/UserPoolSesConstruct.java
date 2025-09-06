package io.stxkxs.execute.aws.cognito;

import io.stxkxs.execute.serialization.Mapper;
import io.stxkxs.execute.serialization.Template;
import io.stxkxs.model._main.Common;
import io.stxkxs.model.aws.cognito.userpool.SesConf;
import io.stxkxs.model.aws.cognito.userpool.UserPoolConf;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awscdk.services.cognito.UserPoolEmail;
import software.amazon.awscdk.services.cognito.UserPoolSESOptions;
import software.constructs.Construct;

@Slf4j
@Getter
public class UserPoolSesConstruct {
  private final UserPoolEmail email;

  @SneakyThrows
  public UserPoolSesConstruct(Construct scope, Common common, UserPoolConf conf) {
    var sesYaml = Template.parse(scope, conf.ses());
    var sesConf = Mapper.get().readValue(sesYaml, SesConf.class);

    log.debug("{} [common: {} conf: {}]", "UserPoolSesConstruct", common, conf);

    if (sesConf.enabled()) {
      this.email =
        UserPoolEmail.withSES(UserPoolSESOptions.builder().fromName(sesConf.sender().fromName()).fromEmail(sesConf.sender().fromEmail())
          .replyTo(sesConf.sender().replyTo()).configurationSetName(sesConf.sender().configurationSetName())
          .sesRegion(sesConf.sender().sesRegion()).sesVerifiedDomain(sesConf.sender().sesVerifiedDomain()).build());
    } else {
      this.email = UserPoolEmail.withCognito();
    }
  }
}
