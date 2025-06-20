package io.stxkxs.execute.aws.dynamodb;

import io.stxkxs.execute.aws.kms.KmsConstruct;
import io.stxkxs.model._main.Common;
import io.stxkxs.model._main.Common.Maps;
import io.stxkxs.model.aws.dynamodb.Index;
import io.stxkxs.model.aws.dynamodb.Owner;
import io.stxkxs.model.aws.dynamodb.Table;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import software.amazon.awscdk.CfnTag;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.services.dynamodb.Attribute;
import software.amazon.awscdk.services.dynamodb.AutoscaledCapacityOptions;
import software.amazon.awscdk.services.dynamodb.Billing;
import software.amazon.awscdk.services.dynamodb.Capacity;
import software.amazon.awscdk.services.dynamodb.GlobalSecondaryIndexPropsV2;
import software.amazon.awscdk.services.dynamodb.LocalSecondaryIndexProps;
import software.amazon.awscdk.services.dynamodb.StreamViewType;
import software.amazon.awscdk.services.dynamodb.TableClass;
import software.amazon.awscdk.services.dynamodb.TableEncryptionV2;
import software.amazon.awscdk.services.dynamodb.TableV2;
import software.amazon.awscdk.services.dynamodb.TableV2.Builder;
import software.amazon.awscdk.services.dynamodb.ThroughputProps;
import software.amazon.awscdk.services.kinesis.Stream;
import software.amazon.awscdk.services.kinesis.StreamEncryption;
import software.amazon.awscdk.services.kinesis.StreamMode;
import software.constructs.Construct;

import static io.stxkxs.execute.serialization.Format.id;

@Slf4j
@Getter
public class DynamoDbConstruct extends Construct {
  private final TableV2 table;

  public DynamoDbConstruct(Construct scope, Common common, Table conf) {
    super(scope, id("dynamodb", conf.name()));

    log.debug("configuration [common: {} table: {}]", common, conf);

    var table = Builder
      .create(this, conf.name())
      .tableName(conf.name())
      .partitionKey(Attribute.builder()
        .name(conf.partitionKey().name())
        .type(conf.partitionKey().type())
        .build())
      .localSecondaryIndexes(conf.localSecondaryIndexes().stream()
        .map(DynamoDbConstruct::localSecondaryIndex)
        .toList())
      .globalSecondaryIndexes(conf.globalSecondaryIndexes().stream()
        .map(DynamoDbConstruct::globalSecondaryIndex)
        .toList())
      .tableClass(TableClass.valueOf(conf.tableClass().toUpperCase()))
      .contributorInsights(conf.contributorInsights())
      .deletionProtection(conf.deletionProtection())
      .pointInTimeRecovery(conf.pointInTimeRecovery())
      .removalPolicy(RemovalPolicy.valueOf(conf.removalPolicy().toUpperCase()))
      .tags(Maps.from(common.tags(), conf.tags())
        .entrySet().stream().map(entry -> CfnTag.builder().key(entry.getKey()).value(entry.getValue()).build())
        .toList());

    sortKey(conf, table);
    encryption(common, conf, table);
    billing(conf, table);
    streams(common, conf, table);

    this.table = table.build();
  }

  private static GlobalSecondaryIndexPropsV2 globalSecondaryIndex(Index index) {
    var gsi = GlobalSecondaryIndexPropsV2.builder()
      .indexName(index.name())
      .projectionType(index.projectionType());

    if (index.sortKey() != null)
      gsi.sortKey(Attribute.builder()
        .name(index.sortKey().name())
        .type(index.sortKey().type())
        .build());

    if (!index.nonKeyAttributes().isEmpty())
      gsi.nonKeyAttributes(index.nonKeyAttributes());

    return gsi.build();
  }

  private static LocalSecondaryIndexProps localSecondaryIndex(Index index) {
    var lsi = LocalSecondaryIndexProps.builder()
      .indexName(index.name())
      .projectionType(index.projectionType());

    if (index.sortKey() != null)
      lsi.sortKey(Attribute.builder()
        .name(index.sortKey().name())
        .type(index.sortKey().type())
        .build());

    if (!index.nonKeyAttributes().isEmpty())
      lsi.nonKeyAttributes(index.nonKeyAttributes());

    return lsi.build();
  }

  private void streams(Common common, Table conf, Builder table) {
    if (conf.streams().kinesis() != null && conf.streams().kinesis().enabled()) {
      var kinesis = conf.streams().kinesis();
      var stream = Stream.Builder.create(this, kinesis.name())
        .streamName(kinesis.name())
        .shardCount(kinesis.shards())
        .streamMode(StreamMode.valueOf(kinesis.mode().toUpperCase()))
        .removalPolicy(RemovalPolicy.valueOf(kinesis.removalPolicy()))
        .retentionPeriod(Duration.days(kinesis.retentionPeriod()));

      var encryption = StreamEncryption.valueOf(kinesis.encryption().toUpperCase());
      if (encryption.equals(StreamEncryption.KMS)) {
        var kms = new KmsConstruct(this, common, kinesis.kms());
        stream.encryptionKey(kms.key());
      }

      table.kinesisStream(stream.build());
    }

    if (conf.streams().dynamoDb() != null && conf.streams().dynamoDb().enabled()) {
      table.dynamoStream(StreamViewType.valueOf(conf.streams().dynamoDb().type().toUpperCase()));
    }
  }

  private void billing(Table conf, Builder table) {
    var ok = BooleanUtils.oneHot(new boolean[]{
      conf.billing().onDemand(),
      conf.billing().fixed() != null,
      conf.billing().provisioned() != null});

    if (ok) {
      if (conf.billing().onDemand()) {
        table.billing(Billing.onDemand());
      }

      if (conf.billing().provisioned() != null) {
        table.billing(Billing.provisioned(
          ThroughputProps.builder()
            .readCapacity(Capacity.autoscaled(
              AutoscaledCapacityOptions.builder()
                .minCapacity(conf.billing().provisioned().read().min())
                .maxCapacity(conf.billing().provisioned().read().max())
                .seedCapacity(conf.billing().provisioned().read().seed())
                .targetUtilizationPercent(conf.billing().provisioned().read().targetUtilizationPercent())
                .build()))
            .writeCapacity(Capacity.autoscaled(AutoscaledCapacityOptions.builder()
              .minCapacity(conf.billing().provisioned().write().min())
              .maxCapacity(conf.billing().provisioned().write().max())
              .seedCapacity(conf.billing().provisioned().write().seed())
              .targetUtilizationPercent(conf.billing().provisioned().write().targetUtilizationPercent())
              .build()))
            .build()));

        if (conf.billing().fixed() != null) {
          table.billing(Billing.provisioned(
            ThroughputProps.builder()
              .readCapacity(Capacity.fixed(conf.billing().fixed().read()))
              .writeCapacity(Capacity.fixed(conf.billing().fixed().write()))
              .build()));
        }
      }
    } else {
      log.error("unable to determine billing type for dynamodb. using the 'ondemand' default.");
      table.billing(Billing.onDemand());
    }
  }

  private void encryption(Common common, Table conf, Builder table) {
    if (conf.encryption().enabled()) {
      var owner = Owner.of(conf.encryption().owner().toUpperCase());
      if (owner.equals(Owner.AWS))
        table.encryption(TableEncryptionV2.awsManagedKey());
      else if (owner.equals(Owner.DYNAMODB))
        table.encryption(TableEncryptionV2.dynamoOwnedKey());
      else if (owner.equals(Owner.SELF) && conf.encryption().kms() != null) {
        var kms = new KmsConstruct(this, common, conf.encryption().kms()).key();
        table.encryption(TableEncryptionV2.customerManagedKey(kms));
      } else {
        log.error("something went wrong while determining the dynamodb encryption key. encryption will be disabled.");
      }
    }
  }

  private void sortKey(Table conf, Builder table) {
    if (conf.sortKey() != null && conf.sortKey().name() != null && conf.sortKey().type() != null)
      table.sortKey(Attribute.builder()
        .name(conf.sortKey().name())
        .type(conf.sortKey().type())
        .build());
  }
}
