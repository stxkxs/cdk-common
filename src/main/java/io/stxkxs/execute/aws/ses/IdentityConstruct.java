package io.stxkxs.execute.aws.ses;

import io.stxkxs.model._main.Common;
import io.stxkxs.model.aws.ses.ConfigurationSetConf;
import io.stxkxs.model.aws.ses.IdentityConf;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awscdk.services.route53.MxRecord;
import software.amazon.awscdk.services.route53.MxRecordValue;
import software.amazon.awscdk.services.route53.PublicHostedZone;
import software.amazon.awscdk.services.route53.PublicHostedZoneAttributes;
import software.amazon.awscdk.services.ses.ConfigurationSet;
import software.amazon.awscdk.services.ses.ConfigurationSetTlsPolicy;
import software.amazon.awscdk.services.ses.DedicatedIpPool;
import software.amazon.awscdk.services.ses.DkimIdentity;
import software.amazon.awscdk.services.ses.EmailIdentity;
import software.amazon.awscdk.services.ses.Identity;
import software.amazon.awscdk.services.ses.MailFromBehaviorOnMxFailure;
import software.amazon.awscdk.services.ses.ScalingMode;
import software.amazon.awscdk.services.ses.SuppressionReasons;
import software.constructs.Construct;

import java.util.List;

@Slf4j
@Getter
public class IdentityConstruct extends Construct {
  private final EmailIdentity hostedZoneIdentity;
  private final ConfigurationSet configurationSet;
  private final EmailIdentity emailIdentity;
  private final MxRecord mxInboundRecord;

  public IdentityConstruct(Construct scope, Common common, IdentityConf conf) {
    super(scope, "ses.identity");

    log.debug("ses identity configuration [common: {} ses-identity: {}]", common, conf);

    var hostedZone = PublicHostedZone
      .fromPublicHostedZoneAttributes(this, "hostedzone.lookup",
        PublicHostedZoneAttributes.builder()
          .zoneName(conf.domain())
          .hostedZoneId(conf.hostedZone())
          .build());

    this.configurationSet = configurationSet(conf.configurationSet());
    this.hostedZoneIdentity = EmailIdentity.Builder
      .create(this, "hostedzone.identity")
      .dkimIdentity(DkimIdentity.easyDkim())
      .configurationSet(this.configurationSet())
      .feedbackForwarding(conf.feedbackForwarding())
      .mailFromBehaviorOnMxFailure(MailFromBehaviorOnMxFailure.valueOf(conf.mxFailure().toUpperCase()))
      .mailFromDomain(conf.mailFromDomain())
      .identity(Identity.publicHostedZone(hostedZone))
      .build();

    this.emailIdentity = EmailIdentity.Builder
      .create(this, conf.email())
      .identity(Identity.email(conf.email()))
      .build();

    this.mxInboundRecord = MxRecord.Builder
      .create(this, "hostedzone.mx.inbound")
      .zone(hostedZone)
      .recordName(String.format("mail.%s", conf.domain()))
      .values(List.of(MxRecordValue.builder()
        .hostName(String.format("inbound-smtp.%s.amazonaws.com", common.region()))
        .priority(10)
        .build()))
      .build();
  }

  private ConfigurationSet configurationSet(ConfigurationSetConf conf) {
    var configurationSet = ConfigurationSet.Builder
      .create(this, conf.name())
      .configurationSetName(conf.name())
      .reputationMetrics(conf.reputationMetrics())
      .sendingEnabled(conf.sendingEnabled())
      .tlsPolicy(ConfigurationSetTlsPolicy.valueOf(conf.tlsPolicyConfiguration().toUpperCase()))
      .suppressionReasons(SuppressionReasons.valueOf(conf.suppressionReasons().toUpperCase()));

    if (conf.dedicatedIpPool().enabled()) {
      configurationSet.dedicatedIpPool(DedicatedIpPool.Builder
        .create(this, "dedicated.ip.pool")
        .dedicatedIpPoolName(conf.dedicatedIpPool().name())
        .scalingMode(ScalingMode.valueOf(conf.dedicatedIpPool().scalingMode().toUpperCase()))
        .build());
    }

    return configurationSet.build();
  }
}
