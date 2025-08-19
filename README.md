# cdk-common

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/projects/jdk/21/)
[![Maven](https://img.shields.io/badge/Maven-3.8+-blue.svg)](https://maven.apache.org/)
[![AWS CDK](https://img.shields.io/badge/AWS%20CDK-2.206.0-yellow.svg)](https://aws.amazon.com/cdk/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](#)
[![Build Status](https://img.shields.io/badge/Build-Passing-brightgreen.svg)](#)

AWS CDK constructs library for enterprise infrastructure.

## Requirements

- Java 21+
- Maven 3.8+
- [AWS CDK](https://aws.amazon.com/cdk/) 2.206.0

## Usage

```xml
<dependency>
    <groupId>io.stxkxs</groupId>
    <artifactId>cdk-common</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Services

**Compute**: Lambda • EKS  
**Storage**: S3 • EBS  
**Database**: DynamoDB • RDS  
**Networking**: VPC • API Gateway • Load Balancers  
**Security**: IAM • Cognito • KMS • Secrets Manager  
**Messaging**: SQS • SNS • SES  
**Analytics**: Athena • Kinesis • MSK  
**Monitoring**: CloudWatch • BCM  
**DevOps**: CodeBuild • ECR

## Build Process

The cdk-common library processes YAML configuration templates during CDK synthesis:

1. **CDK Context Injection** - Context variables from CDK become template variables
2. **Template Resolution** - Templates loaded from `{environment}/{version}/{template-file}` 
3. **Mustache Processing** - Variables substituted using Mustache templating engine
4. **POJO Mapping** - Processed YAML deserialized into Java configuration objects

## Documentation

### 📋 [Build Process](docs/build-process/)
**Process-oriented documentation**
- [Build Process Flow](docs/build-process/build-process.md) - Complete CDK synthesis flow
- [Template System](docs/build-process/template-system.md) - Template resolution and processing
- [Context Variables](docs/build-process/examples/context-variables.md) - Available template variables
- [Template Structure](docs/build-process/examples/template-structure.md) - Organization patterns

### 🏗️ [Architecture](docs/architecture/)
**Decision-oriented documentation**  
- [ADR-001: Template Resolution](docs/architecture/adr-001-template-resolution.md) - Environment/version path structure
- [ADR-002: Mustache Processing](docs/architecture/adr-002-mustache-processing.md) - Template engine choice
- [ADR-003: Context Injection](docs/architecture/adr-003-context-injection.md) - CDK context system

### 👩‍💻 [Developer Workflow](docs/developer-workflow/)
**User-oriented documentation**
- [Developer Guide](docs/developer-workflow/developer-guide.md) - How to use the library
- [Build Internals](docs/developer-workflow/build-internals.md) - What happens during synthesis
- [Troubleshooting](docs/developer-workflow/troubleshooting.md) - Common issues and solutions

### 🔧 [Technical Deep Dive](docs/technical-deep-dive/)
**Implementation-oriented documentation**
- [Template Engine](docs/technical-deep-dive/internals/template-engine.md) - Processing implementation
- [Context System](docs/technical-deep-dive/internals/context-system.md) - CDK context integration
- [Serialization](docs/technical-deep-dive/internals/serialization.md) - Jackson YAML mapping
- [Quick Reference](docs/technical-deep-dive/quick-reference.md) - Template variables and patterns

## Links

- [AWS CDK Documentation](https://docs.aws.amazon.com/cdk/)
- [AWS CDK API Reference](https://docs.aws.amazon.com/cdk/api/v2/)
- [AWS CDK Workshop](https://cdkworkshop.com/)
- [Mustache Templates](https://mustache.github.io/)
