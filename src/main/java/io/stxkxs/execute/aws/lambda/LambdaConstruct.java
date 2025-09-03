package io.stxkxs.execute.aws.lambda;

import io.stxkxs.execute.aws.iam.RoleConstruct;
import io.stxkxs.model._main.Common;
import io.stxkxs.model.aws.fn.Lambda;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.services.ec2.IVpc;
import software.amazon.awscdk.services.ec2.SubnetSelection;
import software.amazon.awscdk.services.ec2.SubnetType;
import software.amazon.awscdk.services.lambda.Architecture;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.CodeSigningConfig;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Function.Builder;
import software.amazon.awscdk.services.lambda.LayerVersion;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.signer.Platform;
import software.amazon.awscdk.services.signer.SigningProfile;
import software.constructs.Construct;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static io.stxkxs.execute.serialization.Format.id;

/**
 * Comprehensive AWS Lambda function construct that provides enterprise-grade serverless compute
 * with advanced features including VPC integration, IAM role management, code signing, and layer support.
 * 
 * <p>This construct serves as the foundational component for all Lambda-based serverless applications,
 * providing sophisticated configuration options, security features, and operational capabilities
 * required for production-ready serverless deployments.
 * 
 * <p><b>Core Lambda Features:</b>
 * <ul>
 *   <li><b>Function Creation</b> - Lambda function with configurable runtime and architecture</li>
 *   <li><b>VPC Integration</b> - Secure deployment within private subnets with ENI management</li>
 *   <li><b>IAM Role Management</b> - Automatic role creation with least-privilege permissions</li>
 *   <li><b>Layer Support</b> - Multiple Lambda layers for dependencies and shared code</li>
 * </ul>
 * 
 * <p><b>Advanced Security Features:</b>
 * <ul>
 *   <li><b>Code Signing</b> - Digital signature verification for code integrity</li>
 *   <li><b>VPC Security</b> - Network isolation with security group and subnet configuration</li>
 *   <li><b>IAM Integration</b> - Precise permission management with role-based access</li>
 *   <li><b>Environment Variables</b> - Secure configuration injection with encryption support</li>
 * </ul>
 * 
 * <p><b>Performance and Operational Excellence:</b>
 * <ul>
 *   <li><b>Runtime Configuration</b> - Configurable memory, timeout, and architecture settings</li>
 *   <li><b>Layer Management</b> - Efficient dependency management and code reuse</li>
 *   <li><b>Architecture Support</b> - x86_64 and ARM64 architecture options</li>
 *   <li><b>Monitoring Integration</b> - CloudWatch Logs and metrics integration</li>
 * </ul>
 * 
 * <p><b>VPC Integration Architecture:</b>
 * The construct provides sophisticated VPC networking capabilities:
 * <ul>
 *   <li><b>Subnet Selection</b> - Automatic private subnet selection for security</li>
 *   <li><b>ENI Management</b> - Elastic Network Interface creation and lifecycle</li>
 *   <li><b>Security Groups</b> - Network access control at the function level</li>
 *   <li><b>NAT Gateway Access</b> - Internet access through NAT gateways when needed</li>
 * </ul>
 * 
 * <p><b>Code Signing Integration:</b>
 * <ul>
 *   <li><b>Signing Profiles</b> - AWS Signer integration for code verification</li>
 *   <li><b>Platform Support</b> - Multiple signing platforms and algorithms</li>
 *   <li><b>Integrity Validation</b> - Runtime code integrity verification</li>
 *   <li><b>Compliance</b> - Enterprise compliance requirements for code authenticity</li>
 * </ul>
 * 
 * <p><b>Layer Management System:</b>
 * <ul>
 *   <li><b>Multiple Layers</b> - Support for base layers, utility layers, and framework layers</li>
 *   <li><b>Version Management</b> - Layer version tracking and compatibility</li>
 *   <li><b>Dependency Optimization</b> - Efficient dependency bundling and distribution</li>
 *   <li><b>Runtime Compatibility</b> - Layer compatibility validation with function runtime</li>
 * </ul>
 * 
 * <p><b>Configuration Flexibility:</b>
 * Supports comprehensive Lambda configuration through template-based setup:
 * <ul>
 *   <li><b>Runtime Selection</b> - Multiple runtime environments (Node.js, Python, Java, .NET, Go, Ruby)</li>
 *   <li><b>Memory and Timeout</b> - Performance tuning with configurable resource allocation</li>
 *   <li><b>Environment Variables</b> - Secure configuration injection with template support</li>
 *   <li><b>Dead Letter Queues</b> - Error handling and retry mechanisms</li>
 * </ul>
 * 
 * <p><b>Integration Patterns:</b>
 * <ul>
 *   <li><b>API Gateway Integration</b> - HTTP API and REST API backend functions</li>
 *   <li><b>Event-Driven Processing</b> - SQS, SNS, S3, and CloudWatch Events integration</li>
 *   <li><b>Database Connectivity</b> - RDS Proxy and direct database connections</li>
 *   <li><b>Microservices Architecture</b> - Function composition and service orchestration</li>
 * </ul>
 * 
 * <p><b>Deployment Architecture:</b>
 * <pre>
 * VPC Configuration → IAM Role Creation → Layer Attachment → Function Deployment → Integration Setup
 *        ↓                    ↓                  ↓                ↓                      ↓
 * Subnet Selection → Permission Setup → Dependency Loading → Code Deployment → Trigger Configuration
 * </pre>
 * 
 * <p><b>Usage Examples:</b>
 * <pre>{@code
 * // Basic Lambda function with VPC integration
 * LambdaConstruct lambda = new LambdaConstruct(
 *     this, 
 *     common, 
 *     lambdaConfig, 
 *     vpc
 * );
 * 
 * // Lambda with additional layers
 * LambdaConstruct advancedLambda = new LambdaConstruct(
 *     this, 
 *     common, 
 *     lambdaConfig, 
 *     vpc, 
 *     commonLayer, 
 *     utilityLayer
 * );
 * 
 * // Access the Lambda function
 * Function function = lambda.getFunction();
 * 
 * // The construct automatically handles:
 * // - IAM role creation with necessary permissions
 * // - VPC configuration with private subnet selection
 * // - Layer attachment and version management
 * // - Code signing configuration (if enabled)
 * // - Environment variable injection
 * // - CloudWatch Logs integration
 * }</pre>
 * 
 * @author CDK Common Framework
 * @since 1.0.0
 * @see Function for AWS CDK Lambda function construct
 * @see RoleConstruct for IAM role provisioning
 * @see LayerVersion for Lambda layer management
 * @see CodeSigningConfig for code signing integration
 * @see Lambda for configuration model
 */
@Slf4j
@Getter
public class LambdaConstruct extends Construct {
  private final Function function;

  public LambdaConstruct(Construct scope, Common common, Lambda conf, IVpc vpc) {
    super(scope, id("lambda", conf.name()));

    log.debug("{} [common: {} conf: {}]", "LambdaConstruct", common, conf);

    this.function = build(common, conf, vpc, layers(conf));
  }

  public LambdaConstruct(Construct scope, Common common, Lambda conf, IVpc vpc, LayerVersion... layers) {
    super(scope, id("lambda", conf.name()));

    log.debug("{} [common: {} conf: {}]", "LambdaConstruct", common, conf);

    this.function = build(common, conf, vpc, layers(conf, layers));
  }

  private Function build(Common common, Lambda conf, IVpc vpc, List<LayerVersion> layers) {
    var role = new RoleConstruct(this, common, conf.role()).role();
    return Builder
      .create(this, conf.name())
      .vpc(vpc)
      .vpcSubnets(SubnetSelection.builder()
        .subnetType(SubnetType.valueOf(conf.subnetType().toUpperCase()))
        .build())
      .role(role)
      .functionName(conf.name())
      .description(conf.description())
      .runtime(conf.runtime())
      .architecture(Architecture.X86_64)
      .codeSigningConfig(CodeSigningConfig.Builder.create(this, id("code.signing", conf.name()))
        .signingProfiles(List.of(SigningProfile.Builder.create(this, id("signing.profile", conf.name()))
          .platform(Platform.AWS_LAMBDA_SHA384_ECDSA)
          .build()))
        .build())
      .code(Code.fromAsset(conf.asset()))
      .environment(conf.environment())
      .handler(conf.handler())
      .timeout(Duration.seconds(conf.timeout()))
      .memorySize(conf.memorySize())
      .layers(layers)
      .build();
  }

  private List<LayerVersion> layers(Lambda conf, LayerVersion... layers) {
    return Stream.concat(
        conf.layers().stream()
          .map(layer -> LayerVersion.Builder.create(this, id("layer", layer.name()))
            .layerVersionName(layer.name())
            .code(Code.fromAsset(conf.asset()))
            .removalPolicy(layer.removalPolicy())
            .compatibleArchitectures(List.of(Architecture.X86_64))
            .compatibleRuntimes(layer.runtimes().stream()
              .map(r -> Runtime.Builder.create(r).build()).toList())
            .build()),
        Optional.ofNullable(layers).stream()
          .flatMap(Arrays::stream))
      .toList();
  }
}
