package io.stxkxs.execute.aws.secretsmanager;

import io.stxkxs.model._main.Common;
import io.stxkxs.model._main.Common.Maps;
import io.stxkxs.model.aws.secretsmanager.SecretCredentials;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Tags;
import software.amazon.awscdk.services.secretsmanager.Secret;
import software.amazon.awscdk.services.secretsmanager.SecretStringGenerator;
import software.constructs.Construct;

import static io.stxkxs.execute.serialization.Format.id;

@Slf4j
@Getter
public class SecretConstruct extends Construct {
  private static final String ignore = "/!@#^~*()_+={};:,.<>]*$\"\\`\\'\\-\\|\\?\\[\\]";

  private final Secret secret;

  @SneakyThrows
  public SecretConstruct(Construct scope, Common common, SecretCredentials conf) {
    super(scope, id("secret", conf.name()));

    log.debug("secret configuration [common: {} secret: ---]", common);

    this.secret = Secret.Builder
      .create(this, conf.name())
      .secretName(conf.name())
      .description(conf.description())
      .generateSecretString(SecretStringGenerator.builder()
        .passwordLength(conf.password().length())
        .excludeNumbers(conf.password().excludeNumbers())
        .excludeLowercase(conf.password().excludeLowercase())
        .excludeUppercase(conf.password().excludeUppercase())
        .includeSpace(conf.password().includeSpace())
        .requireEachIncludedType(conf.password().includeSpace())
        .secretStringTemplate(String.format("{\"username\": \"%s\"}", conf.username()))
        .generateStringKey("password")
        .excludeCharacters(ignore)
        .build())
      .removalPolicy(RemovalPolicy.valueOf(conf.removalPolicy().toUpperCase()))
      .build();

    Maps.from(common.tags(), conf.tags())
      .forEach((key, value) -> Tags.of(secret).add(key, value));
  }
}
