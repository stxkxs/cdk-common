package io.stxkxs.execute.aws.ecr;

import io.stxkxs.execute.aws.kms.KmsConstruct;
import io.stxkxs.model._main.Common;
import io.stxkxs.model.aws.ecr.EcrRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awscdk.Tags;
import software.amazon.awscdk.services.ecr.Repository;
import software.amazon.awscdk.services.ecr.RepositoryEncryption;
import software.constructs.Construct;

import static io.stxkxs.execute.serialization.Format.id;

@Slf4j
@Getter
public class EcrRepositoryConstruct extends Construct {
  private final Repository repository;

  public EcrRepositoryConstruct(Construct scope, Common common, EcrRepository conf) {
    super(scope, id("ecr", conf.name()));

    log.debug("ecr configuration [common: {} repository: {}]", common, conf);

    var ecr = Repository.Builder
      .create(scope, "ecr")
      .repositoryName(conf.name())
      .imageTagMutability(conf.tagMutability())
      .imageScanOnPush(conf.scanOnPush())
      .emptyOnDelete(conf.emptyOnDelete())
      .removalPolicy(conf.removalPolicy());

    if (conf.encryption().enabled()) {
      if (conf.encryption().kms() != null) {
        ecr.encryption(RepositoryEncryption.KMS);
        ecr.encryptionKey(new KmsConstruct(this, common, conf.encryption().kms()).key());
      } else {
        ecr.encryption(RepositoryEncryption.AES_256);
      }
    }

    this.repository = ecr.build();

    common.tags().forEach((k, v) -> Tags.of(this.repository()).add(k, v));
  }
}
