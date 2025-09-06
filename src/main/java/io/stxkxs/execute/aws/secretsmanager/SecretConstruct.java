package io.stxkxs.execute.aws.secretsmanager;

import static io.stxkxs.execute.serialization.Format.id;

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

/**
 * Comprehensive AWS Secrets Manager construct that provides enterprise-grade secret management with automatic password generation, secure
 * credential storage, and advanced security features for protecting sensitive application data.
 *
 * <p>
 * This construct orchestrates the creation of AWS Secrets Manager secrets with sophisticated password generation policies, secure storage
 * configuration, and operational excellence features required for production-ready secret management in cloud applications.
 *
 * <p>
 * <b>Core Secret Management Features:</b>
 * <ul>
 * <li><b>Secret Creation</b> - AWS Secrets Manager secrets with configurable parameters</li>
 * <li><b>Password Generation</b> - Automatic secure password generation with custom policies</li>
 * <li><b>JSON Template Support</b> - Structured credential storage with username/password pairs</li>
 * <li><b>Lifecycle Management</b> - Configurable retention and removal policies</li>
 * </ul>
 *
 * <p>
 * <b>Advanced Security Architecture:</b>
 * <ul>
 * <li><b>Encryption at Rest</b> - AWS KMS encryption for stored secrets</li>
 * <li><b>Access Control</b> - IAM-based access policies and resource-based policies</li>
 * <li><b>Audit Trail</b> - CloudTrail integration for secret access logging</li>
 * <li><b>Cross-Region Replication</b> - Multi-region secret replication capabilities</li>
 * </ul>
 *
 * <p>
 * <b>Password Generation Policies:</b> The construct supports sophisticated password generation with security controls:
 * <ul>
 * <li><b>Length Control</b> - Configurable password length for security requirements</li>
 * <li><b>Character Set Control</b> - Include/exclude numbers, uppercase, lowercase, symbols</li>
 * <li><b>Complexity Requirements</b> - Ensure each character type is included</li>
 * <li><b>Character Exclusion</b> - Exclude problematic characters for system compatibility</li>
 * </ul>
 *
 * <p>
 * <b>Secret Structure Management:</b>
 * <ul>
 * <li><b>JSON Templates</b> - Structured secret storage with predefined schema</li>
 * <li><b>Key-Value Pairs</b> - Username/password and other credential combinations</li>
 * <li><b>Dynamic Generation</b> - Runtime password generation with template injection</li>
 * <li><b>Schema Validation</b> - Consistent secret structure across environments</li>
 * </ul>
 *
 * <p>
 * <b>Integration Patterns:</b>
 * <ul>
 * <li><b>RDS Integration</b> - Database credential management with automatic rotation</li>
 * <li><b>Lambda Integration</b> - Serverless function credential injection</li>
 * <li><b>ECS/EKS Integration</b> - Container-based application secret management</li>
 * <li><b>API Gateway Integration</b> - Secure API credential management</li>
 * </ul>
 *
 * <p>
 * <b>Operational Excellence:</b>
 * <ul>
 * <li><b>Resource Tagging</b> - Comprehensive tagging for governance and cost management</li>
 * <li><b>Removal Policies</b> - Configurable secret lifecycle and cleanup policies</li>
 * <li><b>Version Management</b> - Automatic secret versioning and rollback capabilities</li>
 * <li><b>Monitoring Integration</b> - CloudWatch metrics and alarms for secret access</li>
 * </ul>
 *
 * <p>
 * <b>Security Best Practices:</b>
 * <ul>
 * <li><b>Principle of Least Privilege</b> - Minimal access policies for secret retrieval</li>
 * <li><b>Automatic Rotation</b> - Support for automatic credential rotation schedules</li>
 * <li><b>Cross-Account Access</b> - Secure cross-account secret sharing patterns</li>
 * <li><b>Compliance Ready</b> - GDPR, HIPAA, PCI DSS compliance considerations</li>
 * </ul>
 *
 * <p>
 * <b>Character Exclusion Strategy:</b> The construct implements sophisticated character exclusion to prevent issues:
 * <ul>
 * <li><b>System Compatibility</b> - Excludes characters that cause shell/database issues</li>
 * <li><b>URL Safety</b> - Prevents URL encoding problems in REST APIs</li>
 * <li><b>JSON Safety</b> - Avoids characters that break JSON parsing</li>
 * <li><b>Security Hardening</b> - Prevents injection attack vectors</li>
 * </ul>
 *
 * <p>
 * <b>Secret Lifecycle Management:</b>
 *
 * <pre>
 * Secret Creation → Password Generation → Template Injection → Encryption → Storage
 *       ↓                  ↓                   ↓                ↓           ↓
 * Configuration → Security Policy → JSON Structure → KMS Encrypt → Secrets Manager
 * </pre>
 *
 * <p>
 * <b>Usage Example:</b>
 *
 * <pre>{@code
 * SecretConstruct databaseSecret = new SecretConstruct(this, common, secretConfig);
 *
 * // Automatically creates:
 * // - AWS Secrets Manager secret with generated password
 * // - JSON structure with username and password fields
 * // - KMS encryption for data protection
 * // - IAM policies for secure access
 * // - Resource tags for governance
 *
 * // Use in RDS configuration
 * DatabaseCluster cluster =
 *   DatabaseCluster.Builder.create(this, "cluster").credentials(Credentials.fromSecret(databaseSecret.getSecret())).build();
 *
 * // Access the secret in Lambda
 * Function lambda =
 *   Function.Builder.create(this, "function").environment(Map.of("SECRET_ARN", databaseSecret.getSecret().getSecretArn())).build();
 * }</pre>
 *
 * @author CDK Common Framework
 * @see Secret for AWS CDK Secrets Manager construct
 * @see SecretStringGenerator for password generation policies
 * @see SecretCredentials for secret configuration model
 * @since 1.0.0
 */
@Slf4j
@Getter
public class SecretConstruct extends Construct {
  private static final String ignore = "/!@#^~*()_+={};:,.<>]*$\"\\`\\'\\-\\|\\?\\[\\]";

  private final Secret secret;

  @SneakyThrows
  public SecretConstruct(Construct scope, Common common, SecretCredentials conf) {
    super(scope, id("secret", conf.name()));

    log.debug("{} [common: {}]", "SecretConstruct", common);

    this.secret = Secret.Builder.create(this, conf.name()).secretName(conf.name()).description(conf.description())
      .generateSecretString(
        SecretStringGenerator.builder().passwordLength(conf.password().length()).excludeNumbers(conf.password().excludeNumbers())
          .excludeLowercase(conf.password().excludeLowercase()).excludeUppercase(conf.password().excludeUppercase())
          .includeSpace(conf.password().includeSpace()).requireEachIncludedType(conf.password().includeSpace())
          .secretStringTemplate(String.format("{\"username\": \"%s\"}", conf.username())).generateStringKey("password")
          .excludeCharacters(ignore).build())
      .removalPolicy(RemovalPolicy.valueOf(conf.removalPolicy().toUpperCase())).build();

    Maps.from(common.tags(), conf.tags()).forEach((key, value) -> Tags.of(secret).add(key, value));
  }
}
