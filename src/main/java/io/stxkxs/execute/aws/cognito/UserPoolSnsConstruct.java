package io.stxkxs.execute.aws.cognito;

import io.stxkxs.execute.aws.iam.RoleConstruct;
import io.stxkxs.execute.serialization.Mapper;
import io.stxkxs.execute.serialization.Template;
import io.stxkxs.model._main.Common;
import io.stxkxs.model.aws.cognito.userpool.Sns;
import io.stxkxs.model.aws.cognito.userpool.UserPoolConf;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awscdk.services.iam.Role;
import software.constructs.Construct;

@Slf4j
@Getter
public class UserPoolSnsConstruct {
  private final Role role;
  private final String externalId;

  @SneakyThrows
  public UserPoolSnsConstruct(Construct scope, Common common, UserPoolConf conf) {
    var snsYaml = Template.parse(scope, conf.sns());
    var snsConf = Mapper.get().readValue(snsYaml, Sns.class);

    log.debug("{} [common: {} conf: {}]", "UserPoolSnsConstruct", common, conf);

    this.role = new RoleConstruct(scope, common, snsConf.role()).role();
    this.externalId = snsConf.externalId();
  }
}
