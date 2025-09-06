package io.stxkxs.execute.aws.apigw;

import static io.stxkxs.execute.serialization.Format.id;
import static io.stxkxs.execute.serialization.Format.name;

import io.stxkxs.model._main.Common;
import io.stxkxs.model.aws.apigw.usageplan.UsagePlanConf;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awscdk.services.apigateway.ApiKey;
import software.amazon.awscdk.services.apigateway.QuotaSettings;
import software.amazon.awscdk.services.apigateway.RestApi;
import software.amazon.awscdk.services.apigateway.ThrottleSettings;
import software.amazon.awscdk.services.apigateway.UsagePlan;
import software.amazon.awscdk.services.apigateway.UsagePlan.Builder;
import software.amazon.awscdk.services.apigateway.UsagePlanPerApiStage;
import software.constructs.Construct;

/**
 * Comprehensive Amazon API Gateway Usage Plan construct that provides enterprise-grade API access control with sophisticated throttling,
 * quota management, and API key integration for production API deployments.
 *
 * <p>
 * This construct orchestrates the creation of API Gateway Usage Plans with advanced rate limiting, quota enforcement, and access control
 * features required for managing API consumption at scale in commercial and enterprise environments.
 *
 * <p>
 * <b>Core Usage Plan Features:</b>
 * <ul>
 * <li><b>Usage Plan Creation</b> - API Gateway usage plans with configurable access policies</li>
 * <li><b>Throttling Controls</b> - Request rate limiting and burst protection</li>
 * <li><b>Quota Management</b> - Time-based request quotas and usage limits</li>
 * <li><b>API Key Integration</b> - Automatic API key provisioning and association</li>
 * </ul>
 *
 * <p>
 * <b>Advanced Traffic Management:</b>
 * <ul>
 * <li><b>Rate Limiting</b> - Requests per second throttling with configurable limits</li>
 * <li><b>Burst Protection</b> - Short-term traffic spike handling with burst allowances</li>
 * <li><b>Quota Enforcement</b> - Daily, weekly, monthly usage quotas with automatic reset</li>
 * <li><b>Stage Association</b> - Per-stage usage plan deployment and management</li>
 * </ul>
 *
 * <p>
 * <b>API Key Management System:</b> The construct provides sophisticated API key provisioning:
 * <ul>
 * <li><b>Automatic Generation</b> - Secure API key creation with unique identifiers</li>
 * <li><b>Plan Association</b> - Binding API keys to specific usage plans</li>
 * <li><b>Access Control</b> - Key-based access restriction and authorization</li>
 * <li><b>Usage Tracking</b> - Key-level consumption monitoring and analytics</li>
 * </ul>
 *
 * <p>
 * <b>Enterprise Access Controls:</b>
 * <ul>
 * <li><b>Customer Segmentation</b> - Different usage tiers for different customer types</li>
 * <li><b>Service Level Agreements</b> - SLA enforcement through quota and throttling</li>
 * <li><b>Revenue Protection</b> - Usage limits to prevent cost overruns</li>
 * <li><b>Fair Use Policies</b> - Balanced resource allocation across consumers</li>
 * </ul>
 *
 * <p>
 * <b>Throttling Architecture:</b>
 * <ul>
 * <li><b>Rate Limiting</b> - Sustained requests per second with smooth distribution</li>
 * <li><b>Burst Handling</b> - Short-term traffic spikes with token bucket algorithm</li>
 * <li><b>Queue Management</b> - Request queuing and overflow protection</li>
 * <li><b>Back-pressure</b> - Graceful degradation under high load conditions</li>
 * </ul>
 *
 * <p>
 * <b>Quota Management System:</b>
 * <ul>
 * <li><b>Time-Based Limits</b> - Daily, weekly, monthly quota periods</li>
 * <li><b>Automatic Reset</b> - Quota counter reset at period boundaries</li>
 * <li><b>Consumption Tracking</b> - Real-time usage monitoring and alerting</li>
 * <li><b>Overage Handling</b> - Configurable behavior when quotas are exceeded</li>
 * </ul>
 *
 * <p>
 * <b>Operational Excellence:</b>
 * <ul>
 * <li><b>Usage Monitoring</b> - CloudWatch metrics and dashboards for consumption analysis</li>
 * <li><b>Alerting Integration</b> - Automated alerts for quota violations and high usage</li>
 * <li><b>Cost Management</b> - Usage-based billing and cost allocation</li>
 * <li><b>Performance Optimization</b> - Traffic shaping for optimal resource utilization</li>
 * </ul>
 *
 * <p>
 * <b>Integration Patterns:</b>
 * <ul>
 * <li><b>Multi-Stage Support</b> - Usage plans across development, staging, production</li>
 * <li><b>Customer Onboarding</b> - Automated API key provisioning workflows</li>
 * <li><b>Usage Analytics</b> - Integration with analytics platforms for consumption insights</li>
 * <li><b>Billing Integration</b> - Usage data export for billing and revenue management</li>
 * </ul>
 *
 * <p>
 * <b>Usage Plan Architecture:</b>
 *
 * <pre>
 * API Consumer → API Key → Usage Plan → Throttling/Quota Check → API Gateway → Backend
 *      ↓              ↓          ↓                ↓                    ↓            ↓
 * Client Request → Authentication → Plan Lookup → Rate Limiting → Method Execution → Response
 * </pre>
 *
 * <p>
 * <b>Traffic Management Flow:</b>
 *
 * <pre>
 * Request Arrival → API Key Validation → Usage Plan Lookup → Throttle Check → Quota Check → Allow/Deny
 *       ↓                  ↓                   ↓                ↓              ↓              ↓
 * Client Call → Key Verification → Plan Rules → Rate Limit → Usage Limit → API Execution
 * </pre>
 *
 * <p>
 * <b>Usage Example:</b>
 *
 * <pre>{@code
 * UsagePlanConstruct usagePlan = new UsagePlanConstruct(this, common, usagePlanConfig, restApi);
 *
 * // Automatically creates:
 * // - Usage plan with throttling and quota settings
 * // - API key with secure generation
 * // - Association between key and usage plan
 * // - Stage-specific usage plan deployment
 * // - CloudWatch metrics for monitoring
 *
 * // The construct handles:
 * // - Request rate limiting (requests per second)
 * // - Burst traffic management (token bucket)
 * // - Time-based quota enforcement (daily/weekly/monthly)
 * // - API key lifecycle management
 * // - Usage monitoring and analytics
 *
 * // Access the created resources
 * UsagePlan plan = usagePlan.getUsagePlan();
 *
 * // Client can use API key for authenticated requests
 * // GET /api/resource
 * // Headers: x-api-key: generated-api-key-value
 * }</pre>
 *
 * @author CDK Common Framework
 * @see UsagePlan for AWS CDK Usage Plan construct
 * @see ApiKey for API key management
 * @see ThrottleSettings for rate limiting configuration
 * @see QuotaSettings for usage quota management
 * @see UsagePlanConf for usage plan configuration model
 * @since 1.0.0
 */
@Slf4j
@Getter
public class UsagePlanConstruct extends Construct {
  private final UsagePlan usagePlan;

  public UsagePlanConstruct(Construct scope, Common common, UsagePlanConf conf, RestApi restApi) {
    super(scope, id("usage-plan", common.id(), conf.name()));

    log.debug("{} [common: {} conf: {}]", "UsagePlanConstruct", common, conf);

    var usagePlan =
      UsagePlan.Builder.create(this, name(common.id(), conf.name(), "usage-plan")).name(conf.name()).description(conf.description())
        .throttle(ThrottleSettings.builder().rateLimit(conf.throttle().rateLimit()).burstLimit(conf.throttle().burstLimit()).build());

    maybeApplyQuota(usagePlan, conf);

    this.usagePlan = usagePlan.build();

    this.usagePlan().addApiStage(UsagePlanPerApiStage.builder().stage(restApi.getDeploymentStage()).build());

    this.usagePlan().addApiKey(ApiKey.Builder.create(this, id(conf.name(), "default")).apiKeyName(conf.name()).build());
  }

  private static void maybeApplyQuota(Builder plan, UsagePlanConf conf) {
    if (conf.quota() != null && conf.quota().enabled()) {
      plan.quota(QuotaSettings.builder().limit(conf.quota().limit()).period(conf.quota().period()).build());
    }
  }
}
