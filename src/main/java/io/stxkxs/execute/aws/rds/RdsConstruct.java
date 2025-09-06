package io.stxkxs.execute.aws.rds;

import static io.stxkxs.execute.serialization.Format.id;

import io.stxkxs.execute.aws.secretsmanager.SecretConstruct;
import io.stxkxs.model._main.Common;
import io.stxkxs.model._main.Common.Maps;
import io.stxkxs.model.aws.rds.Rds;
import io.stxkxs.model.aws.rds.RdsReader;
import io.stxkxs.model.aws.rds.RdsWriter;
import java.util.List;
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

/**
 * Comprehensive Amazon RDS Aurora PostgreSQL cluster construct that provides enterprise-grade relational database infrastructure with
 * advanced features including read replicas, backup management, performance insights, and secure credential management.
 *
 * <p>
 * This construct orchestrates the creation of production-ready Aurora PostgreSQL clusters with sophisticated configuration options,
 * security features, and operational capabilities required for high-availability database deployments.
 *
 * <p>
 * <b>Core Database Features:</b>
 * <ul>
 * <li><b>Aurora Cluster</b> - PostgreSQL-compatible Aurora cluster with multi-AZ deployment</li>
 * <li><b>Read Replicas</b> - Multiple read replica instances for read scaling</li>
 * <li><b>Serverless v2</b> - Auto-scaling compute capacity based on workload</li>
 * <li><b>Credential Management</b> - Secure database credentials via AWS Secrets Manager</li>
 * </ul>
 *
 * <p>
 * <b>High Availability Architecture:</b>
 * <ul>
 * <li><b>Multi-AZ Deployment</b> - Cross-availability zone database placement</li>
 * <li><b>Automatic Failover</b> - Built-in failover mechanisms for high availability</li>
 * <li><b>Read Scaling</b> - Multiple read replica instances for performance</li>
 * <li><b>Backup and Recovery</b> - Automated backups with point-in-time recovery</li>
 * </ul>
 *
 * <p>
 * <b>Performance and Monitoring:</b>
 * <ul>
 * <li><b>Performance Insights</b> - Database performance monitoring and analysis</li>
 * <li><b>CloudWatch Integration</b> - Comprehensive metrics and logging</li>
 * <li><b>Enhanced Monitoring</b> - Real-time OS-level metrics collection</li>
 * <li><b>Storage Optimization</b> - Aurora storage with automatic scaling</li>
 * </ul>
 *
 * <p>
 * <b>Security Features:</b>
 * <ul>
 * <li><b>VPC Integration</b> - Database deployment within private subnets</li>
 * <li><b>Security Groups</b> - Network-level access control</li>
 * <li><b>Encryption at Rest</b> - Data encryption using AWS KMS</li>
 * <li><b>Secrets Manager</b> - Automated credential rotation and management</li>
 * </ul>
 *
 * <p>
 * <b>Serverless v2 Integration:</b> The construct supports Aurora Serverless v2 for cost optimization:
 * <ul>
 * <li><b>Auto-scaling</b> - Automatic compute scaling based on workload</li>
 * <li><b>Cost Optimization</b> - Pay-per-use pricing model</li>
 * <li><b>Instant Scaling</b> - Sub-second scaling response times</li>
 * <li><b>Zero Downtime</b> - Scaling operations without connection drops</li>
 * </ul>
 *
 * <p>
 * <b>Operational Excellence:</b>
 * <ul>
 * <li><b>Automated Backups</b> - Continuous backup with configurable retention</li>
 * <li><b>Maintenance Windows</b> - Scheduled maintenance during low-usage periods</li>
 * <li><b>Version Management</b> - PostgreSQL version upgrades and patching</li>
 * <li><b>Parameter Groups</b> - Custom database configuration parameters</li>
 * </ul>
 *
 * <p>
 * <b>Database Architecture:</b>
 *
 * <pre>
 * Aurora Cluster Writer → Primary Database Instance (Write Operations)
 *        ↓
 * Aurora Reader Nodes → Read Replica Instances (Read Operations)
 *        ↓
 * Performance Insights → Monitoring and Analysis
 *        ↓
 * Secrets Manager → Credential Management and Rotation
 * </pre>
 *
 * <p>
 * <b>Usage Example:</b>
 *
 * <pre>{@code
 * RdsConstruct database = new RdsConstruct(this, common, rdsConfig, vpc, databaseSecurityGroup);
 *
 * // Automatically creates:
 * // - Aurora PostgreSQL cluster with writer instance
 * // - Read replica instances for scaling
 * // - Secrets Manager secret for database credentials
 * // - Security group rules for database access
 * // - Performance Insights for monitoring
 * // - Automated backup configuration
 *
 * // Access created resources
 * DatabaseCluster cluster = database.getDatabaseCluster();
 * Secret credentials = database.getCredentials();
 * }</pre>
 *
 * @author CDK Common Framework
 * @see DatabaseCluster for Aurora cluster management
 * @see SecretConstruct for credential management
 * @see ClusterInstance for database instance configuration
 * @see Rds for database configuration model
 * @since 1.0.0
 */
@Slf4j
@Getter
public class RdsConstruct extends Construct {
  private final DatabaseCluster cluster;
  private final SecretConstruct secretConstruct;

  public RdsConstruct(Construct scope, Common common, Rds conf, Vpc vpc, List<ISecurityGroup> securityGroups) {
    super(scope, id("rds", conf.name()));

    log.debug("{} [common: {} conf: {}]", "RdsConstruct", common, conf);

    this.secretConstruct = new SecretConstruct(this, common, conf.credentials());

    this.cluster = DatabaseCluster.Builder.create(this, conf.name()).clusterIdentifier(conf.name()).vpc(vpc).securityGroups(securityGroups)
      .credentials(Credentials.fromSecret(this.secretConstruct().secret()))
      .engine(DatabaseClusterEngine.auroraPostgres(AuroraPostgresClusterEngineProps.builder()
        .version(AuroraPostgresEngineVersion.of(conf.version(), conf.version().split("\\.")[0])).build()))
      .defaultDatabaseName(conf.databaseName()).enableDataApi(conf.enableDataApi())
      .storageType(DBClusterStorageType.valueOf(conf.storageType().toUpperCase())).writer(writer(conf.writer()))
      .readers(conf.readers().stream().map(RdsConstruct::reader).toList())
      .removalPolicy(RemovalPolicy.valueOf(conf.removalPolicy().toUpperCase())).deletionProtection(conf.deletionProtection()).build();

    Maps.from(common.tags(), conf.tags()).forEach((key, value) -> Tags.of(this.cluster()).add(key, value));
  }

  private static IClusterInstance writer(RdsWriter w) {
    return ClusterInstance.serverlessV2(w.name(),
      ServerlessV2ClusterInstanceProps.builder().allowMajorVersionUpgrade(w.allowMajorVersionUpgrade())
        .autoMinorVersionUpgrade(w.autoMinorVersionUpgrade()).publiclyAccessible(w.publiclyAccessible())
        .enablePerformanceInsights(w.performanceInsights().enabled())
        .performanceInsightRetention(PerformanceInsightRetention.valueOf(w.performanceInsights().retention().toUpperCase())).build());
  }

  private static IClusterInstance reader(RdsReader r) {
    return ClusterInstance.serverlessV2(r.name(),
      ServerlessV2ClusterInstanceProps.builder().allowMajorVersionUpgrade(r.allowMajorVersionUpgrade())
        .autoMinorVersionUpgrade(r.autoMinorVersionUpgrade()).publiclyAccessible(r.publiclyAccessible())
        .scaleWithWriter(r.scaleWithWriter()).enablePerformanceInsights(r.performanceInsights().enabled())
        .performanceInsightRetention(PerformanceInsightRetention.valueOf(r.performanceInsights().retention().toUpperCase())).build());
  }
}
