package io.stxkxs.execute.aws.cloudwatch;

import io.stxkxs.execute.aws.kms.KmsConstruct;
import io.stxkxs.model._main.Common;
import io.stxkxs.model._main.Common.Maps;
import io.stxkxs.model.aws.cloudwatch.LogGroupConf;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Tags;
import software.amazon.awscdk.services.logs.ILogGroup;
import software.amazon.awscdk.services.logs.LogGroup;
import software.amazon.awscdk.services.logs.LogGroupClass;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.constructs.Construct;

import java.util.Optional;

import static io.stxkxs.execute.serialization.Format.id;

@Slf4j
@Getter
public class LogGroupConstruct extends Construct {
  private final ILogGroup logGroup;

  public LogGroupConstruct(Construct scope, Common common, LogGroupConf conf) {
    super(scope, id("log-group", conf.name()));

    log.debug("log group configuration [common: {} log-group: {}]", common, conf);

    var builder = LogGroup.Builder
      .create(this, id("cloudwatch.log-group", conf.name()))
      .logGroupName(conf.name())
      .logGroupClass(LogGroupClass.valueOf(conf.type().toUpperCase()))
      .retention(RetentionDays.valueOf(conf.retention().toUpperCase()))
      .removalPolicy(RemovalPolicy.valueOf(conf.removalPolicy().toUpperCase()));

    Optional.ofNullable(conf.kms())
      .map(k -> new KmsConstruct(this, common, k).key())
      .map(builder::encryptionKey);

    this.logGroup = builder.build();

    Maps.from(conf.tags(), common.tags())
      .forEach((k, v) -> Tags.of(this.logGroup()).add(k, v));
  }
}
