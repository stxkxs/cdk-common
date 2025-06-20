package io.stxkxs.execute.aws.rds;

import io.stxkxs.execute.aws.secretsmanager.SecretConstruct;
import io.stxkxs.model._main.Common;
import io.stxkxs.model._main.Common.Maps;
import io.stxkxs.model.aws.rds.Rds;
import io.stxkxs.model.aws.rds.RdsReader;
import io.stxkxs.model.aws.rds.RdsWriter;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Tags;
import software.amazon.awscdk.services.ec2.ISecurityGroup;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.rds.AuroraPostgresClusterEngineProps;
import software.amazon.awscdk.services.rds.AuroraPostgresEngineVersion;
import software.amazon.awscdk.services.rds.ClusterInstance;
import software.amazon.awscdk.services.rds.Credentials;
import software.amazon.awscdk.services.rds.DBClusterStorageType;
import software.amazon.awscdk.services.rds.DatabaseCluster;
import software.amazon.awscdk.services.rds.DatabaseClusterEngine;
import software.amazon.awscdk.services.rds.IClusterInstance;
import software.amazon.awscdk.services.rds.PerformanceInsightRetention;
import software.amazon.awscdk.services.rds.ServerlessV2ClusterInstanceProps;
import software.constructs.Construct;

import java.util.List;

import static io.stxkxs.execute.serialization.Format.id;

@Slf4j
@Getter
public class RdsConstruct extends Construct {
  private final DatabaseCluster cluster;
  private final SecretConstruct secretConstruct;

  public RdsConstruct(Construct scope, Common common, Rds conf, Vpc vpc, List<ISecurityGroup> securityGroups) {
    super(scope, id("rds", conf.name()));

    log.debug("rds configuration [common: {} rds: {}]", common, conf);

    this.secretConstruct = new SecretConstruct(this, common, conf.credentials());

    this.cluster = DatabaseCluster.Builder
      .create(this, conf.name())
      .clusterIdentifier(conf.name())
      .vpc(vpc)
      .securityGroups(securityGroups)
      .credentials(Credentials.fromSecret(this.secretConstruct().secret()))
      .engine(DatabaseClusterEngine.auroraPostgres(
        AuroraPostgresClusterEngineProps.builder()
          .version(AuroraPostgresEngineVersion.of(conf.version(), conf.version().split("\\.")[0]))
          .build()))
      .defaultDatabaseName(conf.databaseName())
      .enableDataApi(conf.enableDataApi())
      .storageType(DBClusterStorageType.valueOf(conf.storageType().toUpperCase()))
      .writer(writer(conf.writer()))
      .readers(conf.readers().stream()
        .map(RdsConstruct::reader)
        .toList())
      .removalPolicy(RemovalPolicy.valueOf(conf.removalPolicy().toUpperCase()))
      .deletionProtection(conf.deletionProtection())
      .build();

    Maps.from(common.tags(), conf.tags())
      .forEach((key, value) -> Tags.of(this.cluster()).add(key, value));
  }

  private static IClusterInstance writer(RdsWriter w) {
    return ClusterInstance.serverlessV2(w.name(),
      ServerlessV2ClusterInstanceProps.builder()
        .allowMajorVersionUpgrade(w.allowMajorVersionUpgrade())
        .autoMinorVersionUpgrade(w.autoMinorVersionUpgrade())
        .publiclyAccessible(w.publiclyAccessible())
        .enablePerformanceInsights(w.performanceInsights().enabled())
        .performanceInsightRetention(PerformanceInsightRetention.valueOf(w.performanceInsights().retention().toUpperCase()))
        .build());
  }

  private static IClusterInstance reader(RdsReader r) {
    return ClusterInstance.serverlessV2(r.name(),
      ServerlessV2ClusterInstanceProps.builder()
        .allowMajorVersionUpgrade(r.allowMajorVersionUpgrade())
        .autoMinorVersionUpgrade(r.autoMinorVersionUpgrade())
        .publiclyAccessible(r.publiclyAccessible())
        .scaleWithWriter(r.scaleWithWriter())
        .enablePerformanceInsights(r.performanceInsights().enabled())
        .performanceInsightRetention(PerformanceInsightRetention.valueOf(r.performanceInsights().retention().toUpperCase()))
        .build());
  }
}
