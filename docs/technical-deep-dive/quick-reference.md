# Quick Reference Guide

## Template Variable Reference

### Default Context Variables

#### Host Variables (Physical Infrastructure)

| Variable                | Description            | Example                   |
|-------------------------|------------------------|---------------------------|
| `{{host:id}}`           | Application identifier | `myapp`                   |
| `{{host:organization}}` | Organization name      | `acme`                    |
| `{{host:account}}`      | AWS account ID         | `123456789012`            |
| `{{host:region}}`       | AWS region             | `us-east-1`               |
| `{{host:name}}`         | Application name       | `my-application`          |
| `{{host:alias}}`        | Environment alias      | `dev`, `prod`             |
| `{{host:environment}}`  | Environment type       | `prototype`, `production` |
| `{{host:version}}`      | Template version       | `v1`, `v2`, `v3`          |
| `{{host:domain}}`       | Base domain            | `example.com`             |

#### Hosted Variables (Logical Services)

| Variable                  | Description          | Example                   |
|---------------------------|----------------------|---------------------------|
| `{{hosted:id}}`           | Service identifier   | `myapp-api`               |
| `{{hosted:organization}}` | Service organization | `acme-corp`               |
| `{{hosted:account}}`      | Target AWS account   | `123456789012`            |
| `{{hosted:region}}`       | Target AWS region    | `us-east-1`               |
| `{{hosted:name}}`         | Service name         | `user-service`            |
| `{{hosted:alias}}`        | Service alias        | `api`, `web`              |
| `{{hosted:environment}}`  | Service environment  | `prototype`, `production` |
| `{{hosted:version}}`      | Service version      | `v1`, `v2`, `v3`          |
| `{{hosted:domain}}`       | Service domain       | `api.example.com`         |

#### Computed Variables

| Variable               | Description          | Default               |
|------------------------|----------------------|-----------------------|
| `{{home}}`             | Base template path   | `/`                   |
| `{{synthesizer:name}}` | CDK synthesizer name | `default-synthesizer` |

## Common Template Patterns

### Resource Naming

```yaml
# Standard naming pattern
name: "{{hosted:id}}-{{service}}"
# Result: myapp-api-database

# Organization-based naming  
bucketName: "{{hosted:organization}}-{{hosted:name}}-{{hosted:alias}}-data"
# Result: acme-corp-user-service-api-data

# Role ARN construction
roleArn: "arn:aws:iam::{{hosted:account}}:role/{{hosted:id}}-role"
# Result: arn:aws:iam::123456789012:role/myapp-api-role
```

### Tagging Patterns

```yaml
tags:
  "{{hosted:domain}}:resource-type": "bucket"
  "{{hosted:domain}}:component": "{{hosted:id}}-storage"
  "{{hosted:domain}}:part-of": "{{hosted:organization}}.{{hosted:name}}.{{hosted:alias}}"
  "{{hosted:domain}}:environment": "{{hosted:environment}}"

# Results in:
# api.example.com:resource-type: bucket
# api.example.com:component: myapp-api-storage  
# api.example.com:part-of: acme-corp.user-service.api
# api.example.com:environment: prototype
```

### Conditional Content

```yaml
# Environment-specific configuration
{{#production}}
deletionProtection: true
backupRetentionPeriod: 30
instanceType: db.r6g.large
{{/production}}

{{#prototype}}
deletionProtection: false
backupRetentionPeriod: 1
instanceType: db.t3.micro
{{/prototype}}
```

### Nested Template References

```yaml
# Reference other templates
deployment:
  network: "vpc/network.mustache"
  database: "rds/postgres.mustache"
  auth: "cognito/userpool.mustache"

# With custom policies
customPolicies:
  - name: "{{hosted:id}}-s3-access"
    policy: "policy/s3-bucket-access.mustache"
    mappings:
      bucketName: "{{hosted:organization}}-{{hosted:name}}-data"
```

## Template Directory Structure

### Standard Layout

```
src/main/resources/
├── {environment}/              # prototype, production  
│   └── {version}/              # v1, v2, v3
│       ├── conf.mustache       # Main configuration
│       ├── api/
│       │   └── gateway.mustache
│       ├── auth/
│       │   ├── userpool.mustache
│       │   └── cognito.mustache
│       ├── database/
│       │   ├── postgres.mustache
│       │   └── dynamodb.mustache
│       ├── network/
│       │   ├── vpc.mustache
│       │   └── security-groups.mustache
│       └── policy/
│           ├── s3-access.mustache
│           └── lambda-execution.mustache
```

### Template Categories

#### Service Templates

- **Purpose:** Configure specific AWS services
- **Location:** `{service}/` subdirectories
- **Examples:** `rds/database.mustache`, `eks/cluster.mustache`

#### Policy Templates

- **Purpose:** IAM policies and permissions
- **Location:** `policy/` directory
- **Examples:** `policy/s3-access.mustache`, `policy/lambda-execution.mustache`

#### Static Resources

- **Purpose:** Non-templated YAML/JSON files
- **Location:** Mixed with templates
- **Examples:** `eks/storage-class.yaml`

## CDK Integration Patterns

### Basic Template Usage

```java
// Simple template parsing
var config = Template.parse(scope, "database.mustache");
var databaseConf = Mapper.get().readValue(config, DatabaseConf.class);

// With custom variables
var config = Template.parse(scope, "database.mustache", 
  Map.of("database:version", "15.4"));
```

### Construct Integration

```java
public class DatabaseConstruct extends Construct {
  public DatabaseConstruct(Scope scope, Common common, String templatePath) {
    super(scope, Format.id("database", common.name()));
    
    // Process template into configuration
    var yaml = Template.parse(scope, templatePath);
    var config = Mapper.get().readValue(yaml, DatabaseConf.class);
    
    // Create AWS resources using configuration
    var database = RdsInstance.Builder.create(this, "instance")
      .engine(DatabaseEngine.postgres(PostgresEngineVersion.of(config.engineVersion())))
      .instanceType(InstanceType.of(config.instanceClass()))
      // ... other configuration
      .build();
  }
}
```

### Context Configuration

```json
// cdk.context.json
{
  "host:id": "myapp",
  "host:environment": "prototype",
  "host:version": "v1",
  "host:account": "123456789012",
  "host:region": "us-east-1",
  "hosted:id": "myapp-api",
  "hosted:organization": "acme",
  "hosted:name": "user-service"
}
```

## Error Handling Quick Reference

### Common Errors

#### Template Not Found

```
error parsing template! can not find prototype/v1/missing.mustache.
```

**Fix:** Create template file or check environment/version context

#### Missing Context Variable

```
java.lang.NullPointerException: Cannot invoke "toString()" because 
the return value of "getContext(String)" is null
```

**Fix:** Add missing context variable to `cdk.context.json`

#### YAML Syntax Error

```
com.fasterxml.jackson.dataformat.yaml.snakeyaml.error.YAMLException:
  mapping values are not allowed here
```

**Fix:** Check YAML indentation and syntax

#### Unrecognized Property

```
com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException:
  Unrecognized field "invalidField"
```

**Fix:** Add field to Java class or remove from YAML

### Debug Commands

```bash
# Enable CDK debug output
export CDK_DEBUG=true
cdk synth

# Test specific context
cdk synth --context host:environment=production

# Validate templates
cdk synth --validation
```

## Performance Tips

### Template Optimization

- Keep templates small and focused
- Minimize nested template references
- Use efficient POJO structures
- Cache frequently used configurations

### Context Optimization

- Set context at appropriate scope level
- Avoid redundant context variables
- Use computed variables for derived values
- Validate required context early

### Build Optimization

- Use incremental builds when possible
- Minimize template processing in constructors
- Batch similar resource creation
- Use CDK aspects for cross-cutting concerns
