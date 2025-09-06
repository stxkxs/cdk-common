# Developer Guide

## Getting Started

This guide explains how developers use the cdk-common library in their CDK projects to build AWS infrastructure with
templated configurations.

## Project Setup

### 1. Add Dependency

```xml
<dependency>
    <groupId>io.stxkxs</groupId>
    <artifactId>cdk-common</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. Create Template Directory Structure

```
src/main/resources/
└── prototype/              # Start with prototype environment
    └── v1/                # Use v1 for initial version
        ├── conf.mustache  # Main configuration template
        └── [service]/     # Service-specific templates
            └── *.mustache
```

### 3. Set CDK Context

```json
// cdk.context.json
{
  "host:id": "myapp",
  "host:environment": "prototype", 
  "host:version": "v1",
  "host:account": "123456789012",
  "host:region": "us-east-1",
  "host:organization": "acme",
  "host:name": "my-application",
  "host:alias": "dev",
  "host:domain": "example.com",
  
  "hosted:id": "myapp-dev",
  "hosted:organization": "acme-corp",
  "hosted:account": "123456789012",
  "hosted:region": "us-east-1", 
  "hosted:name": "my-service",
  "hosted:alias": "dev",
  "hosted:environment": "prototype",
  "hosted:version": "v1",
  "hosted:domain": "dev.example.com"
}
```

## Development Workflow

### 1. Create Main Configuration Template

```yaml
# src/main/resources/prototype/v1/conf.mustache
host:
  common:
    id: "{{host:id}}"
    name: "{{host:name}}"
    account: "{{host:account}}"
    region: "{{host:region}}"
    organization: "{{host:organization}}"
    alias: "{{host:alias}}"
    environment: "{{host:environment}}"
    domain: "{{host:domain}}"

hosted:
  common:
    id: "{{hosted:id}}"
    organization: "{{hosted:organization}}"
    account: "{{hosted:account}}"
    region: "{{hosted:region}}"
    name: "{{hosted:name}}"
    alias: "{{hosted:alias}}"
    environment: "{{hosted:environment}}"
    domain: "{{hosted:domain}}"

deployment:
  # Reference service-specific templates
  vpc: "vpc/network.mustache"
  database: "rds/database.mustache"
```

### 2. Create Service Templates

```yaml
# src/main/resources/prototype/v1/rds/database.mustache
engine: postgres
engineVersion: "15.4"
instanceClass: db.t3.micro
allocatedStorage: 20
dbName: "{{hosted:id}}_db"
username: app_user
deletionProtection: false
tags:
  "{{hosted:domain}}:resource-type": "database"
  "{{hosted:domain}}:component": "{{hosted:id}}-rds"
  "{{hosted:domain}}:part-of": "{{hosted:organization}}.{{hosted:name}}.{{hosted:alias}}"
```

### 3. Create CDK Stack

```java
// Launch.java
public class Launch {
  public static void main(String[] args) {
    var app = new App();
    var conf = loadConfiguration(app);
    
    new MyStack(app, conf.hosted(),
      StackProps.builder()
        .stackName(Format.name(conf.hosted().common().id(), "infrastructure"))
        .env(Environment.builder()
          .account(conf.hosted().common().account())
          .region(conf.hosted().common().region())
          .build())
        .build());
        
    app.synth();
  }
  
  @SneakyThrows
  private static Hosted<Bare, MyConfig> loadConfiguration(App app) {
    var parsed = Template.parse(app, "conf.mustache");
    return Mapper.get().readValue(parsed, new TypeReference<Hosted<Bare, MyConfig>>() {});
  }
}
```

### 4. Use Constructs with Templates

```java
// In your stack
public class MyStack extends Stack {
  public MyStack(Scope scope, Hosted<Bare, MyConfig> conf, StackProps props) {
    super(scope, conf.common().id(), props);
    
    // cdk-common constructs automatically process templates
    var database = new RdsConstruct(this, conf.common(), conf.deployment().database());
    var vpc = new VpcConstruct(this, conf.common(), conf.deployment().vpc());
  }
}
```

## Testing Your Templates

### 1. Validate Template Syntax

```bash
# Test CDK synthesis
cdk synth

# Check for template parsing errors
# Missing templates will show clear error messages
```

### 2. Verify Variable Substitution

```bash
# Enable debug logging to see processed templates
export CDK_DEBUG=true
cdk synth
```

### 3. Test Different Environments

```bash
# Test with production context
cdk synth --context host:environment=production --context host:version=v1
```

## Environment Progression

### Prototype → Production

1. **Create Production Templates**

```
src/main/resources/
├── prototype/v1/...        # Development configs
└── production/v1/...       # Production configs (copy and modify)
```

2. **Update Production Context**

```json
// cdk.context.json for production
{
  "host:environment": "production",
  "host:alias": "prod",
  "hosted:environment": "production", 
  "hosted:alias": "prod",
  "hosted:domain": "api.example.com"
}
```

3. **Production-Specific Changes**

```yaml
# production/v1/rds/database.mustache
engine: postgres
engineVersion: "15.4"
instanceClass: db.r6g.large        # Larger instance
allocatedStorage: 100              # More storage
deletionProtection: true           # Enable protection
```

## Common Patterns

### Custom Variables

```java
// Add service-specific context
var parsed = Template.parse(this, "database.mustache",
  Map.ofEntries(
    Map.entry("database:version", "15.4"),
    Map.entry("database:backup:retention", "30")
  ));
```

### Conditional Templates

```yaml
# Use Mustache sections for conditional content
{{#production}}
deletionProtection: true
backupRetentionPeriod: 30
{{/production}}

{{#development}}
deletionProtection: false
backupRetentionPeriod: 1
{{/development}}
```

### Template Composition

```yaml
# conf.mustache - Reference other templates
deployment:
  network: "vpc/network.mustache"
  security: "iam/policies.mustache" 
  monitoring: "cloudwatch/alarms.mustache"
```

## Troubleshooting

See [troubleshooting.md](troubleshooting.md) for common issues and solutions.