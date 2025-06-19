package io.stxkxs.execute.aws.cognito;

import io.stxkxs.execute.serialization.Mapper;
import io.stxkxs.execute.serialization.Template;
import io.stxkxs.model._main.Common;
import io.stxkxs.model._main.Common.Maps;
import io.stxkxs.model.aws.cognito.client.ClientAttributesConf;
import io.stxkxs.model.aws.cognito.client.UserPoolClientConf;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Tags;
import software.amazon.awscdk.services.cognito.AuthFlow;
import software.amazon.awscdk.services.cognito.ClientAttributes;
import software.amazon.awscdk.services.cognito.OAuthFlows;
import software.amazon.awscdk.services.cognito.OAuthScope;
import software.amazon.awscdk.services.cognito.OAuthSettings;
import software.amazon.awscdk.services.cognito.StandardAttributesMask;
import software.amazon.awscdk.services.cognito.UserPool;
import software.amazon.awscdk.services.cognito.UserPoolClient;
import software.amazon.awscdk.services.cognito.UserPoolClientOptions;
import software.constructs.Construct;

import java.security.InvalidParameterException;
import java.util.List;

import static io.stxkxs.execute.serialization.Format.id;

@Slf4j
@Getter
public class UserPoolClientConstruct extends Construct {
  private final UserPoolClient userPoolClient;

  @SneakyThrows
  public UserPoolClientConstruct(Construct scope, Common common, String path, UserPool parent) {
    super(scope, id("user-pool-client", common.name()));

    var yaml = Template.parse(scope, path);
    var conf = Mapper.get().readValue(yaml, UserPoolClientConf.class);

    log.debug("userpool client configuration [common: {} userpool-client: {}]", common, conf);

    this.userPoolClient = parent.addClient(
      conf.name(),
      UserPoolClientOptions.builder()
        .userPoolClientName(conf.name())
        .readAttributes(attributes(conf.readAttributes(), conf.customAttributes()))
        .writeAttributes(attributes(conf.writeAttributes(), conf.customAttributes()))
        .accessTokenValidity(Duration.parse(conf.accessTokenValidity()))
        .authSessionValidity(Duration.parse(conf.authSessionValidity()))
        .refreshTokenValidity(Duration.parse(conf.refreshTokenValidity()))
        .idTokenValidity(Duration.parse(conf.idTokenValidity()))
        .enableTokenRevocation(conf.enableTokenRevocation())
        .generateSecret(conf.generateSecret())
        .disableOAuth(conf.disableOAuth())
        .preventUserExistenceErrors(conf.preventUserExistenceErrors())
        .oAuth(OAuthSettings.builder()
          .flows(OAuthFlows.builder()
            .implicitCodeGrant(conf.oauth().implicitCodeGrant())
            .clientCredentials(conf.oauth().clientCredentials())
            .authorizationCodeGrant(conf.oauth().authorizationCodeGrant())
            .build())
          .scopes(conf.oauth().scopes().stream()
            .map(UserPoolClientConstruct::scope)
            .toList())
          .callbackUrls(conf.oauth().callbackUrls())
          .logoutUrls(conf.oauth().logoutUrls())
          .build())
        .authFlows(AuthFlow.builder()
          .adminUserPassword(conf.authFlow().adminUserPassword())
          .userPassword(conf.authFlow().userPassword())
          .userSrp(conf.authFlow().userSrp())
          .custom(conf.authFlow().custom())
          .build())
        .build());

    Maps.from(common.tags(), conf.tags()).forEach((key, value) -> Tags.of(parent).add(key, value));
  }

  private static ClientAttributes attributes(ClientAttributesConf conf, List<String> customAttributes) {
    return new ClientAttributes()
      .withStandardAttributes(
        StandardAttributesMask.builder()
          .address(conf.address())
          .birthdate(conf.birthdate())
          .email(conf.email())
          .emailVerified(conf.email_verified())
          .familyName(conf.family_name())
          .fullname(conf.name())
          .gender(conf.gender())
          .givenName(conf.given_name())
          .lastUpdateTime(conf.updated_at())
          .locale(conf.locale())
          .middleName(conf.middle_name())
          .nickname(conf.nickname())
          .phoneNumber(conf.phone_number())
          .phoneNumberVerified(conf.phone_number_verified())
          .preferredUsername(conf.preferred_username())
          .profilePage(conf.profile_page())
          .profilePicture(conf.profile_picture())
          .timezone(conf.timezone())
          .website(conf.website())
          .build())
      .withCustomAttributes(customAttributes.toArray(new String[0]));
  }

  private static OAuthScope scope(String scope) {
    enum types {EMAIL, PHONE, COGNITO_ADMIN, PROFILE, OPENID}

    if (scope.equalsIgnoreCase(types.EMAIL.name())) {
      return OAuthScope.EMAIL;
    } else if (scope.equalsIgnoreCase(types.PHONE.name())) {
      return OAuthScope.PHONE;
    } else if (scope.equalsIgnoreCase(types.COGNITO_ADMIN.name())) {
      return OAuthScope.COGNITO_ADMIN;
    } else if (scope.equalsIgnoreCase(types.PROFILE.name())) {
      return OAuthScope.PROFILE;
    } else if (scope.equalsIgnoreCase(types.OPENID.name())) {
      return OAuthScope.OPENID;
    }

    throw new InvalidParameterException("error deciding oauth scope type for user pool client");
  }
}

