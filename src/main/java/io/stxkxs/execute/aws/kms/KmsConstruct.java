package io.stxkxs.execute.aws.kms;

import io.stxkxs.model._main.Common;
import io.stxkxs.model.aws.kms.Kms;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Tags;
import software.amazon.awscdk.services.kms.Key;
import software.amazon.awscdk.services.kms.KeySpec;
import software.amazon.awscdk.services.kms.KeyUsage;
import software.constructs.Construct;

import static io.stxkxs.execute.serialization.Format.id;

@Slf4j
@Getter
public class KmsConstruct extends Construct {
  private final Key key;

  public KmsConstruct(Construct scope, Common common, Kms conf) {
    super(scope, id("kms", conf.alias()));

    log.debug("{} [common: {} conf: {}]", "KmsConstruct", common, conf);

    this.key = Key.Builder
      .create(this, conf.alias())
      .alias(conf.alias())
      .description(conf.description())
      .enabled(conf.enabled())
      .enableKeyRotation(conf.enableKeyRotation())
      .keyUsage(KeyUsage.valueOf(conf.keyUsage().toUpperCase()))
      .keySpec(KeySpec.valueOf(conf.keySpec().toUpperCase()))
      .removalPolicy(RemovalPolicy.valueOf(conf.removalPolicy().toUpperCase()))
      .build();

    common.tags().forEach((k, v) ->
      Tags.of(this.key()).add(k, v));
  }
}
