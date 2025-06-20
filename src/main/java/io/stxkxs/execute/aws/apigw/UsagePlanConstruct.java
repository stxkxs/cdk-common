package io.stxkxs.execute.aws.apigw;

import io.stxkxs.model._main.Common;
import io.stxkxs.model.aws.apigw.usageplan.UsagePlanConf;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awscdk.services.apigateway.ApiKey;
import software.amazon.awscdk.services.apigateway.QuotaSettings;
import software.amazon.awscdk.services.apigateway.RestApi;
import software.amazon.awscdk.services.apigateway.ThrottleSettings;
import software.amazon.awscdk.services.apigateway.UsagePlan;
import software.amazon.awscdk.services.apigateway.UsagePlan.Builder;
import software.amazon.awscdk.services.apigateway.UsagePlanPerApiStage;
import software.constructs.Construct;

import static io.stxkxs.execute.serialization.Format.id;
import static io.stxkxs.execute.serialization.Format.name;

@Slf4j
@Getter
public class UsagePlanConstruct extends Construct {
  private final UsagePlan usagePlan;

  public UsagePlanConstruct(Construct scope, Common common, UsagePlanConf conf, RestApi restApi) {
    super(scope, id("usage-plan", common.id(), conf.name()));

    log.debug("rest api usage plan configuration [common: {} usage-plan: {}]", common, conf);

    var usagePlan = UsagePlan.Builder
      .create(this, name(common.id(), conf.name(), "usage-plan"))
      .name(conf.name())
      .description(conf.description())
      .throttle(ThrottleSettings.builder()
        .rateLimit(conf.throttle().rateLimit())
        .burstLimit(conf.throttle().burstLimit())
        .build());

    maybeApplyQuota(usagePlan, conf);

    this.usagePlan = usagePlan.build();

    this.usagePlan().addApiStage(
      UsagePlanPerApiStage.builder()
        .stage(restApi.getDeploymentStage())
        .build());

    this.usagePlan().addApiKey(
      ApiKey.Builder.create(this, id(conf.name(), "default"))
        .apiKeyName(conf.name())
        .build());
  }

  private static void maybeApplyQuota(Builder plan, UsagePlanConf conf) {
    if (conf.quota() != null && conf.quota().enabled()) {
      plan.quota(QuotaSettings.builder()
        .limit(conf.quota().limit())
        .period(conf.quota().period())
        .build());
    }
  }
}
