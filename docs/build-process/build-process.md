# CDK Build Process Flow

## Overview

This document explains what happens when AWS CDK builds projects using the cdk-common library. The build process follows a specific flow: **CDK Context → Template Resolution → Mustache Processing → POJO Mapping**.

## Build Flow

### 1. CDK Context Injection

When CDK runs (`cdk synth` or `cdk deploy`), it provides context variables to constructs:

```java
// Template.java:71-88 - Default context variables extracted
var version = Version.of(scope.getNode().getContext("host:version"));
var environment = Environment.of(scope.getNode().getContext("host:environment"));
```

**Key Context Variables:**
- `host:*` - Host environment details (account, region, domain)
- `hosted:*` - Hosted service details (organization, name, alias)
- `home` - Base path for templates
- `synthesizer:name` - CDK synthesizer identifier

### 2. Template Path Resolution

Templates are resolved using a structured path format:

```java
// Template.java:42-43
var prefix = String.format("%s/%s", environment, version);
var template = String.format("%s/%s", prefix, file);
```

**Path Structure:**
```
src/main/resources/
├── {environment}/          # e.g., "prototype", "production"
│   └── {version}/          # e.g., "v1", "v2", "v3"
│       ├── conf.mustache
│       ├── eks/
│       │   ├── addons.mustache
│       │   └── node-groups.mustache
│       └── policy/
│           └── karpenter.mustache
```

### 3. Template Loading

Templates are loaded from the classpath:

```java
// Template.java:45
try (var stream = Template.class.getClassLoader().getResourceAsStream(template)) {
  // Process template
}
```

**Error Handling:** If template not found, runtime exception thrown with path details.

### 4. Mustache Processing

Templates are processed using Mustache with context variables:

```java
// Template.java:98-100
factory.compile(new InputStreamReader(stream, StandardCharsets.UTF_8), template)
  .execute(writer, values)
  .flush();
```

**Variable Substitution:** `{{hosted:id}}`, `{{host:account}}`, etc. replaced with actual values.

### 5. YAML → POJO Mapping

Processed YAML is deserialized into Java POJOs using Jackson:

```java
// Various constructs
var addons = Mapper.get().readValue(Template.parse(scope, conf.addons()), AddonsConf.class);
```

**Jackson Configuration:**
- Case-insensitive property matching
- Custom accessor naming (no prefixes)
- Non-null serialization
- YAML format support

## Example Flow

**Input Context:**
```json
{
  "host:environment": "prototype",
  "host:version": "v1", 
  "hosted:id": "myapp",
  "hosted:account": "123456789012"
}
```

**Template Path:** `prototype/v1/eks/addons.mustache`

**Template Content:**
```yaml
managed:
  awsVpcCni:
    name: vpc-cni
    serviceAccount:
      role:
        name: {{hosted:id}}-vpc-cni
```

**After Processing:**
```yaml
managed:
  awsVpcCni:
    name: vpc-cni
    serviceAccount:
      role:
        name: myapp-vpc-cni
```

**Final POJO:** `AddonsConf` Java object with populated fields.

## Key Files

- `Template.java:23` - Main template parsing entry point
- `Template.java:71-88` - Default context variable extraction  
- `Mapper.java:25-43` - Jackson YAML configuration
- Consumer `Launch.java:49` - Template parsing with custom variables