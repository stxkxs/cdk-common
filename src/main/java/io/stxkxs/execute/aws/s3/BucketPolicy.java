package io.stxkxs.execute.aws.s3;

import com.fasterxml.jackson.core.type.TypeReference;
import io.stxkxs.execute.serialization.Mapper;
import io.stxkxs.execute.serialization.Template;
import io.stxkxs.model.aws.iam.Principal;
import io.stxkxs.model.aws.s3.BucketPolicyConf;
import io.stxkxs.model.aws.s3.BucketPolicyStatementConf;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awscdk.services.iam.Effect;
import software.amazon.awscdk.services.iam.PolicyStatement;
import software.constructs.Construct;

@Slf4j
@Getter
public class BucketPolicy {

  public static PolicyStatement policyStatement(Construct scope, BucketPolicyConf conf) {
    log.debug("s3 bucket policy configuration [bucket-policy: {}]", conf);

    var s = parse(scope, conf);
    return PolicyStatement.Builder.create().sid(s.sid()).effect(Effect.valueOf(s.effect().toUpperCase()))
      .principals(conf.principals().stream().map(Principal::iamPrincipal).toList()).actions(s.actions()).resources(s.resources())
      .conditions(s.conditions()).build();
  }

  @SneakyThrows
  public static BucketPolicyStatementConf parse(Construct scope, BucketPolicyConf conf) {
    var parsed = Template.parse(scope, conf.policy(), conf.mappings());
    return Mapper.get().readValue(parsed, new TypeReference<>() {});
  }
}
