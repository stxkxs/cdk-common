# ADR-003: CDK Context Variable System

## Status  
Accepted

## Context
The cdk-common library needs a mechanism to provide dynamic values to templates during CDK synthesis. These values must include deployment context (environment, account, region) and be accessible across all constructs in a consistent manner.

## Decision
We will use CDK's built-in context system to inject variables into templates, with a standardized variable naming convention.

**Variable Categories:**
- `host:*` - Host environment metadata  
- `hosted:*` - Hosted service metadata
- `synthesizer:*` - CDK synthesizer information

**Implementation:**
```java
// Template.java:71-88 - Context extraction
Map.entry("host:id", scope.getNode().getContext("host:id").toString()),
Map.entry("host:account", scope.getNode().getContext("host:account").toString()),
Map.entry("hosted:organization", scope.getNode().getContext("hosted:organization").toString())
```

## Rationale

### Why CDK Context?

#### 1. Native Integration
- **CDK Built-in:** Leverages CDK's existing context mechanism
- **Inheritance:** Context automatically flows to child constructs  
- **CLI Support:** Can be set via `cdk.context.json` or command line
- **Type Safety:** Context values accessible through construct tree

#### 2. Deployment-Time Resolution
```java
// Values resolved during CDK synthesis
var version = Version.of(scope.getNode().getContext("host:version"));
var environment = Environment.of(scope.getNode().getContext("host:environment"));
```

**Benefits:**
- Same template works across environments with different context
- No hardcoded values in templates
- Dynamic resource naming based on deployment context

#### 3. Hierarchical Variable System

**Host Variables (Physical Infrastructure):**
```yaml
host:id: "myapp"              # Application identifier
host:account: "123456789012"  # AWS account
host:region: "us-east-1"      # AWS region  
host:environment: "production" # Deployment environment
host:domain: "example.com"    # Base domain
```

**Hosted Variables (Logical Services):**
```yaml
hosted:id: "myapp-api"           # Service identifier
hosted:organization: "acme"      # Organization name
hosted:name: "user-service"      # Service name
hosted:account: "123456789012"   # Target account (may differ from host)
hosted:domain: "api.example.com" # Service domain
```

### Variable Naming Convention

#### Namespace Strategy
- **Prefix-based:** `host:`, `hosted:`, `synthesizer:`
- **Hierarchical:** Colons separate namespace levels
- **Descriptive:** Clear meaning without documentation

#### Examples
```yaml
# Resource naming patterns
bucket: "{{hosted:organization}}-{{hosted:name}}-{{hosted:alias}}-data"
role: "{{hosted:id}}-service-role"

# ARN construction
roleArn: "arn:aws:iam::{{hosted:account}}:role/{{hosted:id}}-role"

# Tagging patterns  
tags:
  "{{hosted:domain}}:component": "{{hosted:id}}-storage"
  "{{hosted:domain}}:part-of": "{{hosted:organization}}.{{hosted:name}}.{{hosted:alias}}"
```

## Consequences

### Positive
- **Consistency:** Standardized variable names across all templates
- **Flexibility:** Same template works in multiple environments  
- **CDK Integration:** Leverages existing CDK functionality
- **Inheritance:** Child constructs automatically get parent context
- **Tooling:** Standard CDK tools work with context variables

### Negative  
- **CDK Dependency:** Tied to CDK context system
- **Naming Overhead:** Longer variable names with namespaces
- **Context Management:** Teams must manage context values correctly

### Alternatives Considered

#### Environment Variables
```java
System.getenv("HOSTED_ID")
```
**Rejected:** Not deployment-specific, no CDK integration, harder to manage

#### Configuration Files  
```yaml
# config.yaml
hosted:
  id: myapp-api
  account: "123456789012"
```
**Rejected:** Separate config management, no CDK context flow

#### Hard-coded Values
```yaml  
name: myapp-vpc-cni
account: "123456789012"
```
**Rejected:** No flexibility, environment-specific templates required

#### Property Files
```properties
hosted.id=myapp-api
hosted.account=123456789012
```
**Rejected:** Different format, no hierarchical structure

## Implementation Notes

### Context Sources
1. **cdk.context.json** - Project-level defaults
2. **CLI Arguments** - `cdk deploy --context host:environment=production`  
3. **App Code** - `app.node.setContext("host:id", "myapp")`
4. **Stack Code** - Stack-specific context overrides

### Default Values
```java
// Template.java:57-66 - Default value handling
var home = Optional.of(scope)
  .map(s -> s.getNode().tryGetContext("home"))
  .map(Object::toString)
  .orElse("/");
```

### Custom Context Variables
```java
// Launch.java:49-52 - Additional context per construct
Template.parse(app, "conf.mustache",
  Map.ofEntries(
    Map.entry("hosted:eks:druid:release", "v24.0.0"),
    Map.entry("hosted:tags", tags(app))
  ));
```

### Error Handling
- **Missing Context:** `getContext()` throws if required value missing
- **Type Conversion:** Context values converted to strings for templates
- **Validation:** Construct-level validation of context values