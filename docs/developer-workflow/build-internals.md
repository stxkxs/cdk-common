# Build Internals: What Happens During CDK Synthesis

## Overview

This document explains the internal processes that occur when you run `cdk synth` or `cdk deploy` with projects using the cdk-common library.

## CDK Synthesis Flow

### 1. CDK App Initialization
```bash
cdk synth
```

**What Happens:**
1. CDK reads `cdk.json` configuration
2. CDK loads context from `cdk.context.json` 
3. CDK instantiates your `App` class
4. Context variables become available to constructs

```java
// Your Launch.java
var app = new App();
// Context is now available: app.getNode().getContext("host:id")
```

### 2. Template Resolution Phase

**Code Path:** `Template.parse(scope, "conf.mustache")`

```java
// Template.java:23-26
var version = Version.of(scope.getNode().getContext("host:version"));     // "v1"
var environment = Environment.of(scope.getNode().getContext("host:environment")); // "prototype"
return execute(environment, version, file, defaults(scope));
```

**Template Path Construction:**
```java
// Template.java:42-43  
var prefix = String.format("%s/%s", environment, version);  // "prototype/v1"
var template = String.format("%s/%s", prefix, file);        // "prototype/v1/conf.mustache"
```

**ClassLoader Resolution:**
```java
// Template.java:45
var stream = Template.class.getClassLoader().getResourceAsStream(template);
// Searches: src/main/resources/prototype/v1/conf.mustache
```

### 3. Context Variable Extraction

**Default Variables:** (Template.java:71-88)
```java
Map.ofEntries(
  Map.entry("host:id", scope.getNode().getContext("host:id").toString()),           // "myapp"
  Map.entry("host:account", scope.getNode().getContext("host:account").toString()), // "123456789012"
  Map.entry("hosted:domain", scope.getNode().getContext("hosted:domain").toString()) // "api.example.com"
  // ... 18 total default variables
);
```

**Runtime Context:**
```java
// Template.java:58-66
var home = Optional.of(scope)
  .map(s -> s.getNode().tryGetContext("home"))
  .map(Object::toString)
  .orElse("/");  // Default fallback
```

### 4. Mustache Template Processing

**Template Compilation:**
```java
// Template.java:98-100
var factory = new DefaultMustacheFactory();
factory.compile(new InputStreamReader(stream, StandardCharsets.UTF_8), template)
  .execute(writer, values)  // Variable substitution happens here
  .flush();
```

**Variable Substitution Example:**
```yaml
# Input template
name: {{hosted:id}}-bucket
region: {{hosted:region}}

# With context: hosted:id="myapp", hosted:region="us-east-1"
# Output after processing
name: myapp-bucket  
region: us-east-1
```

### 5. YAML → POJO Deserialization

**Jackson Processing:**
```java
// Various constructs use this pattern
var config = Mapper.get().readValue(Template.parse(scope, "conf.mustache"), ConfigClass.class);
```

**Jackson Configuration:** (Mapper.java:25-43)
```java
JsonMapper.builder(yamlConf())
  .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)      // Case-insensitive enums
  .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES) // Case-insensitive properties
  .serializationInclusion(Include.NON_NULL)                 // Skip null values
  .addMixIn(Object.class, DefaultMixin.class)               // Default serialization
  .accessorNaming(new Provider()                            // No getter/setter prefixes
    .withGetterPrefix("")
    .withSetterPrefix(""))
```

### 6. Construct Creation

**Construct Instantiation:**
```java
// Your stack code
var database = new RdsConstruct(this, common, databaseConfig);
```

**Internal Processing:**
1. Construct receives processed POJO configuration
2. Creates AWS CDK resources based on configuration
3. Applies tags, naming conventions from template variables
4. Establishes resource dependencies

### 7. CloudFormation Generation

**CDK Synthesis:**
1. All constructs contribute resources to CDK tree
2. CDK synthesizes CloudFormation templates
3. Templates written to `cdk.out/` directory
4. Asset management (Docker images, Lambda code) handled

## Performance Characteristics

### Template Caching
- **No explicit caching** - templates processed on each construct creation
- **ClassLoader efficiency** - repeated resource access is optimized by JVM
- **Template compilation** - Mustache compiles templates but doesn't cache them

### Memory Usage
- **Template content** - Loaded into memory during processing
- **Context variables** - Shared across all constructs in scope tree
- **POJO objects** - Created per construct, garbage collected after use

### Build Time Impact
```
Template Processing Time:
├── File I/O: ~1-5ms per template
├── Mustache processing: ~1-3ms per template  
├── YAML parsing: ~2-8ms per template
└── Total per template: ~5-20ms
```

**Optimization Strategies:**
- Keep templates small and focused
- Minimize nested template references
- Use efficient POJO structures

## Error Handling During Build

### Template Not Found
```java
// Template.java:46-49
if (stream == null) {
  var m = String.format("error parsing template! can not find %s.", template);
  throw new RuntimeException(m);
}
```

**Error Message Example:**
```
error parsing template! can not find prototype/v1/missing-template.mustache.
```

### Context Variable Missing
```java
// When getContext() called on missing variable
Exception in thread "main" java.lang.NullPointerException: 
  Cannot invoke "toString()" because the return value of 
  "software.constructs.Node.getContext(String)" is null
```

### YAML Parsing Errors
```
com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException:
  Unrecognized field "invalidField" (class io.stxkxs.model.RdsConf)
```

### Mustache Processing Errors
```
com.github.mustachejava.MustacheException: 
  Template 'prototype/v1/conf.mustache' not found
```

## Debug Information

### Enable Debug Logging
```bash
export CDK_DEBUG=true
cdk synth
```

### Template Processing Logs
```java
// Template.java:38,91 - Debug output
log.debug("parsing template {}/{}/{} with parameters {}", environment, version, file, values);
log.debug("default template variables [defaults: {}]", defaults);
```

### Inspect Generated CloudFormation
```bash
# View generated templates
cat cdk.out/MyStack.template.json

# View CDK metadata
cat cdk.out/manifest.json
```