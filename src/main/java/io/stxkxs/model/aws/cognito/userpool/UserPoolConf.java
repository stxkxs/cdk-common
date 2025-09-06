package io.stxkxs.model.aws.cognito.userpool;

import java.util.List;
import java.util.Map;

/**
 * Comprehensive AWS Cognito User Pool configuration record that defines complete user authentication, authorization, and management
 * settings for identity and access management.
 *
 * <p>
 * This configuration record orchestrates all aspects of Cognito User Pool setup including user registration, authentication flows,
 * multi-factor authentication, custom attributes, external service integrations, and advanced security features.
 *
 * <p>
 * <b>Core Authentication Features:</b>
 * <ul>
 * <li><b>Multi-Factor Authentication</b> - SMS, TOTP, and hardware token support</li>
 * <li><b>Password Policies</b> - Complexity requirements and rotation policies</li>
 * <li><b>Sign-in Methods</b> - Username, email, phone number authentication</li>
 * <li><b>Account Recovery</b> - Password reset and account recovery workflows</li>
 * <li><b>User Verification</b> - Email and SMS verification processes</li>
 * </ul>
 *
 * <p>
 * <b>User Management Capabilities:</b>
 * <ul>
 * <li><b>Standard Attributes</b> - AWS-defined user attributes (email, name, etc.)</li>
 * <li><b>Custom Attributes</b> - Application-specific user data fields</li>
 * <li><b>User Groups</b> - Role-based grouping and permissions</li>
 * <li><b>Device Tracking</b> - Remember and track user devices</li>
 * <li><b>Self Registration</b> - Allow users to create their own accounts</li>
 * </ul>
 *
 * <p>
 * <b>External Service Integration:</b>
 * <ul>
 * <li><b>SES Integration</b> - Amazon SES for email delivery and verification</li>
 * <li><b>SNS Integration</b> - Amazon SNS for SMS messaging and notifications</li>
 * <li><b>Lambda Triggers</b> - Custom business logic integration at auth events</li>
 * <li><b>Advanced Threat Protection</b> - AWS WAF and IP blocking integration</li>
 * </ul>
 *
 * <p>
 * <b>Security and Compliance:</b>
 * <ul>
 * <li><b>Deletion Protection</b> - Prevent accidental user pool deletion</li>
 * <li><b>Case Sensitivity</b> - Configure username case handling</li>
 * <li><b>Threat Protection</b> - Automated security threat detection and mitigation</li>
 * <li><b>Attribute Preservation</b> - Control which attributes to keep during updates</li>
 * </ul>
 *
 * <p>
 * <b>Configuration Complexity:</b> This record represents one of the most complex authentication configurations in AWS, involving multiple
 * interrelated settings that affect user experience, security posture, and integration patterns across web and mobile applications.
 *
 * <p>
 * <b>Template Integration:</b> Most fields support template-based configuration allowing dynamic injection of environment-specific values,
 * ARNs, and computed configuration parameters at deployment time through the CDK framework's template processing engine.
 *
 * <p>
 * <b>Usage Example:</b>
 *
 * <pre>{@code
 * UserPoolConf config = new UserPoolConf("my-app-users", // name
 *   triggerConfigJson, // Lambda triggers
 *   "CONFIRMED_AND_VERIFIED", // account recovery
 *   "ENFORCED", // threat protection
 *   snsConfigJson, // SNS integration
 *   sesConfigJson, // SES integration
 *   List.of(adminGroup, userGroup), // user groups
 *   autoVerifyConfig, // verification settings
 *   mfaConfig, // MFA configuration
 *   passwordPolicyConfig, // password rules
 *   verificationConfig, // verification methods
 *   deviceTrackingConfig, // device management
 *   signInAliasesConfig, // sign-in options
 *   standardAttributeMap, // AWS standard attributes
 *   customAttributesList, // custom user attributes
 *   keepOriginalConfig, // attribute preservation
 *   false, // case sensitive usernames
 *   true, // deletion protection
 *   true, // allow self signup
 *   "RETAIN", // removal policy
 *   tagMap // resource tags
 * );
 * }</pre>
 *
 * @param name
 *          Unique name identifier for the Cognito User Pool
 * @param triggers
 *          JSON configuration for Lambda trigger integrations
 * @param accountRecovery
 *          Account recovery method configuration
 * @param standardThreatProtectionMode
 *          AWS advanced threat protection settings
 * @param sns
 *          JSON configuration for SNS integration and SMS delivery
 * @param ses
 *          JSON configuration for SES integration and email delivery
 * @param groups
 *          List of user groups with roles and permissions
 * @param autoVerify
 *          Automatic verification settings for email and phone
 * @param mfa
 *          Multi-factor authentication configuration and requirements
 * @param passwordPolicy
 *          Password complexity and security requirements
 * @param verification
 *          User verification method configuration
 * @param deviceTracking
 *          Device memory and tracking settings
 * @param aliases
 *          Allowed sign-in aliases (username, email, phone)
 * @param standardAttributes
 *          Configuration for AWS standard user attributes
 * @param customAttributes
 *          List of application-specific custom user attributes
 * @param keepOriginalAttributes
 *          Which attributes to preserve during user updates
 * @param signInCaseSensitive
 *          Whether usernames are case sensitive
 * @param deletionProtection
 *          Protect user pool from accidental deletion
 * @param selfSignup
 *          Allow users to register themselves
 * @param removalPolicy
 *          CDK removal policy for user pool deletion
 * @param tags
 *          Resource tags for billing, management, and organization
 * @author CDK Common Framework
 * @see Mfa for multi-factor authentication configuration
 * @see PasswordPolicy for password security requirements
 * @see UserAttribute for standard attribute configuration
 * @see CustomAttribute for application-specific attributes
 * @since 1.0.0
 */
public record UserPoolConf(String name, String triggers, String accountRecovery, String standardThreatProtectionMode, String sns,
  String ses, List<Group> groups, AutoVerify autoVerify, Mfa mfa, PasswordPolicy passwordPolicy, UserVerification verification,
  DeviceTracking deviceTracking, SignInAliases aliases, Map<StandardAttributeKey, UserAttribute> standardAttributes,
  List<CustomAttribute> customAttributes, KeepOriginalAttributes keepOriginalAttributes, boolean signInCaseSensitive,
  boolean deletionProtection, boolean selfSignup, String removalPolicy, Map<String, String> tags) {}
