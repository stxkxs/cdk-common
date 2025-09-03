package io.stxkxs.model.aws.apigw;

import io.stxkxs.model.aws.apigw.usageplan.UsagePlanConf;
import io.stxkxs.model.aws.cloudwatch.LogGroupConf;
import io.stxkxs.model.aws.fn.LambdaLayer;
import io.stxkxs.model.aws.fn.RequestModel;
import io.stxkxs.model.aws.fn.RequestValidator;
import software.amazon.awscdk.services.apigateway.AuthorizationType;

import java.util.List;
import java.util.Map;

/**
 * Comprehensive Amazon API Gateway REST API configuration record that defines complete
 * API infrastructure with advanced features for enterprise-grade serverless applications.
 * 
 * <p>This configuration orchestrates all aspects of API Gateway setup including Lambda integration,
 * request validation, authorization, monitoring, usage plans, and performance optimization
 * for production-ready API deployments.
 * 
 * <p><b>Core API Infrastructure:</b>
 * <ul>
 *   <li><b>VPC Integration</b> - Private API deployment within specified VPC</li>
 *   <li><b>Lambda Architecture</b> - Serverless compute integration with configurable runtime</li>
 *   <li><b>Base Layer Support</b> - Common dependencies and utilities across Lambda functions</li>
 *   <li><b>CloudWatch Integration</b> - Comprehensive logging and monitoring capabilities</li>
 * </ul>
 * 
 * <p><b>Security and Access Control:</b>
 * <ul>
 *   <li><b>API Key Management</b> - Key-based access control and throttling</li>
 *   <li><b>Authorization Types</b> - IAM, Cognito, Lambda authorizers, and custom schemes</li>
 *   <li><b>Execute API Control</b> - Fine-grained API execution permissions</li>
 *   <li><b>Usage Plans</b> - Rate limiting, quota management, and billing integration</li>
 * </ul>
 * 
 * <p><b>Request/Response Management:</b>
 * <ul>
 *   <li><b>Request Validation</b> - Schema-based input validation and sanitization</li>
 *   <li><b>Request Models</b> - JSON schema definitions for API documentation and validation</li>
 *   <li><b>Method Responses</b> - Structured response templates and status code mapping</li>
 *   <li><b>Content Negotiation</b> - Multi-format request/response handling</li>
 * </ul>
 * 
 * <p><b>Operational Excellence:</b>
 * <ul>
 *   <li><b>Stage Configuration</b> - Environment-specific deployment stages and variables</li>
 *   <li><b>Log Group Management</b> - Centralized logging with retention and filtering</li>
 *   <li><b>Performance Optimization</b> - Caching, compression, and response optimization</li>
 *   <li><b>Deployment Automation</b> - Blue/green deployments and canary releases</li>
 * </ul>
 * 
 * <p><b>Integration Patterns:</b>
 * This configuration supports sophisticated API integration scenarios:
 * <ul>
 *   <li>Microservices orchestration with multiple Lambda backends</li>
 *   <li>Event-driven architectures with async processing</li>
 *   <li>API-first development with OpenAPI/Swagger integration</li>
 *   <li>Multi-tenant SaaS applications with usage-based billing</li>
 * </ul>
 * 
 * <p><b>Usage Plan Architecture:</b>
 * The configuration supports complex usage plan hierarchies with:
 * <ul>
 *   <li>Per-customer rate limiting and quotas</li>
 *   <li>Tiered pricing models with different API access levels</li>
 *   <li>Burst capacity and throttling controls</li>
 *   <li>Integration with AWS billing and metering systems</li>
 * </ul>
 * 
 * <p><b>Monitoring and Observability:</b>
 * <ul>
 *   <li><b>CloudWatch Metrics</b> - Request counts, latency, error rates</li>
 *   <li><b>Access Logging</b> - Detailed request/response logging with custom formats</li>
 *   <li><b>X-Ray Integration</b> - Distributed tracing for performance analysis</li>
 *   <li><b>Custom Metrics</b> - Business-specific metrics and KPI tracking</li>
 * </ul>
 * 
 * <p><b>Usage Example:</b>
 * <pre>{@code
 * ApiConf config = new ApiConf(
 *     "production-vpc",                        // VPC name
 *     "user-management-api",                   // API name
 *     "User management microservice API",      // description
 *     commonLambdaLayer,                       // shared layer
 *     "arm64",                                 // architecture
 *     true,                                    // CloudWatch enabled
 *     false,                                   // allow execute API
 *     true,                                    // require API key
 *     AuthorizationType.COGNITO_USER_POOLS,    // authorization
 *     List.of(premiumPlan, basicPlan),         // usage plans
 *     List.of(bodyValidator, paramValidator),   // validators
 *     List.of(userModel, responseModel),       // request models
 *     List.of(successResponse, errorResponse), // method responses
 *     stageConfiguration,                      // stage options
 *     logGroupConfig,                          // logging setup
 *     resourceTags                             // AWS tags
 * );
 * }</pre>
 * 
 * @param vpcName Name of the VPC for private API deployment
 * @param name Unique identifier for the API Gateway REST API
 * @param description Human-readable description of the API purpose and functionality
 * @param baseLayer Common Lambda layer with shared dependencies and utilities
 * @param architecture Lambda runtime architecture (x86_64 or arm64)
 * @param cloudwatchEnabled Enable CloudWatch logging and monitoring integration
 * @param disableExecuteApi Disable default execute API endpoint for enhanced security
 * @param apiKeyRequired Require API key for all requests (used with usage plans)
 * @param authorizationType Primary authorization mechanism for API methods
 * @param usagePlans List of usage plans defining rate limits, quotas, and pricing tiers
 * @param validators List of request validators for input validation and sanitization
 * @param requestModels List of JSON schema models for request/response structure
 * @param methodResponses List of method response configurations for different HTTP status codes
 * @param stageOptions Deployment stage configuration including variables and settings
 * @param logGroup CloudWatch log group configuration for API access and execution logs
 * @param tags AWS resource tags for billing, management, and organizational purposes
 * 
 * @author CDK Common Framework
 * @since 1.0.0
 * @see UsagePlanConf for usage plan and throttling configuration
 * @see RequestValidator for input validation setup
 * @see RequestModel for JSON schema model definitions
 * @see MethodResponse for response template configuration
 * @see StageOptions for deployment stage management
 */
public record ApiConf(
  String vpcName,
  String name,
  String description,
  LambdaLayer baseLayer,
  String architecture,
  boolean cloudwatchEnabled,
  boolean disableExecuteApi,
  boolean apiKeyRequired,
  AuthorizationType authorizationType,
  List<UsagePlanConf> usagePlans,
  List<RequestValidator> validators,
  List<RequestModel> requestModels,
  List<MethodResponse> methodResponses,
  StageOptions stageOptions,
  LogGroupConf logGroup,
  Map<String, String> tags
) {}
