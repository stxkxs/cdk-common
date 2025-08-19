# Template Engine Implementation Details

## Core Components

### Template.java - Main Processing Engine

#### Static Methods Overview
```java
// Template.java:23 - Primary entry point
public static String parse(Construct scope, String file)

// Template.java:30 - With custom variables 
public static String parse(Construct scope, String file, Map<String, Object> values)

// Template.java:37 - Internal execution
private static String execute(Environment environment, Version version, String file, Map<String, Object> values)
```

#### Path Resolution Logic
```java
// Template.java:42-43
var prefix = String.format("%s/%s", environment, version);
var template = String.format("%s/%s", prefix, file);
```

**Resolution Strategy:**
1. Extract `environment` and `version` from CDK context
2. Construct path: `{environment}/{version}/{file}`
3. Load via ClassLoader from resources directory

**Example Paths:**
- Input: `"eks/addons.mustache"`, Context: `environment=prototype, version=v1`
- Result: `"prototype/v1/eks/addons.mustache"`

#### Resource Loading Mechanism
```java
// Template.java:45-52
try (var stream = Template.class.getClassLoader().getResourceAsStream(template)) {
  if (stream == null) {
    var m = String.format("error parsing template! can not find %s.", template);
    throw new RuntimeException(m);
  }
  assemble(stream, template, values, factory, writer);
}
```

**ClassLoader Resolution:**
- Uses thread context class loader
- Searches classpath for resources
- Supports JAR packaging and IDE development
- Returns `null` if resource not found

#### Default Context Variable Extraction
```java
// Template.java:57-94 - Context variable mapping
protected static Map<String, Object> defaults(Construct scope) {
  var home = Optional.of(scope)
    .map(s -> s.getNode().tryGetContext("home"))
    .map(Object::toString)
    .orElse("/");

  var synthesizer = Optional.of(scope)
    .map(s -> s.getNode().tryGetContext("hosted:synthesizer:name"))
    .map(Object::toString)
    .orElseGet(Common::id_);

  return Map.ofEntries(
    // 18 total context variables extracted
    Map.entry("host:id", scope.getNode().getContext("host:id").toString()),
    Map.entry("hosted:account", scope.getNode().getContext("hosted:account").toString()),
    // ... remaining variables
  );
}
```

**Variable Categories:**
- **Required Variables:** Throw NPE if missing (via `getContext()`)
- **Optional Variables:** Use fallbacks (via `tryGetContext()`)
- **Computed Variables:** Generated during processing

### Mustache Integration

#### Template Compilation Process
```java
// Template.java:97-101
protected static void assemble(InputStream stream, String template, Map<String, Object> values, 
                               DefaultMustacheFactory factory, StringWriter writer) {
  factory.compile(new InputStreamReader(stream, StandardCharsets.UTF_8), template)
    .execute(writer, values)
    .flush();
}
```

**Mustache Factory Configuration:**
- Uses `DefaultMustacheFactory` with default settings
- No custom delimiters or special configuration
- UTF-8 encoding for international character support
- Template compilation happens per-invocation (no caching)

#### Variable Resolution Strategy
```java
// Variables resolved in order:
// 1. Custom variables (passed to parse method)
// 2. Default context variables (extracted from CDK context)
// 3. Mustache built-in functions (if any)
```

**Variable Precedence:**
```java
// Template.java:33 - Maps.from merges with custom variables taking precedence
return execute(environment, version, file, Maps.from(defaults(scope), values));
```

### Error Handling Implementation

#### Missing Template Handling
```java
// Template.java:46-49
if (stream == null) {
  var m = String.format("error parsing template! can not find %s.", template);
  throw new RuntimeException(m);
}
```

**Error Information:**
- **Full Path:** Complete template path for debugging
- **Runtime Exception:** Fails fast during CDK synthesis
- **Stack Trace:** Shows calling construct for context

#### Context Variable Errors
```java
// Template.java:71-88 - Various getContext() calls
Map.entry("host:id", scope.getNode().getContext("host:id").toString())
```

**Failure Modes:**
- **NullPointerException:** When required context missing
- **toString() Failure:** When context value is null
- **ClassCastException:** When context value wrong type

### Performance Characteristics

#### Template Processing Performance
```java
// Per-template overhead:
// 1. ClassLoader resource lookup: ~1-5ms
// 2. Stream I/O: ~1-3ms  
// 3. Mustache compilation: ~1-5ms
// 4. Variable substitution: ~1-3ms
// Total: ~5-20ms per template
```

#### Memory Usage Patterns
- **Template Content:** Loaded into memory during processing
- **Context Variables:** Shared across construct tree
- **Mustache Objects:** Created per template, eligible for GC
- **Output Strings:** Generated and passed to Jackson

#### Optimization Opportunities
1. **Template Caching:** Could cache compiled Mustache templates
2. **Context Caching:** Could cache extracted context variables
3. **Resource Pooling:** Could reuse StringWriter instances

### Integration Points

#### CDK Context Integration
```java
// Template.java:24-25,31-32
var version = Version.of(scope.getNode().getContext("host:version"));
var environment = Environment.of(scope.getNode().getContext("host:environment"));
```

**Context Flow:**
1. CDK App sets context values
2. Context flows down construct tree
3. Template engine extracts at point of use
4. Variables become available to Mustache

#### Jackson Integration
```java
// Typical usage pattern in constructs:
var parsed = Template.parse(scope, "config.mustache");
var config = Mapper.get().readValue(parsed, ConfigClass.class);
```

**Processing Pipeline:**
1. Template engine produces YAML string
2. Jackson parses YAML into Java objects
3. Constructs use typed configuration objects

### Debugging and Observability

#### Debug Logging
```java
// Template.java:38,91
log.debug("parsing template {}/{}/{} with parameters {}", environment, version, file, values);
log.debug("default template variables [defaults: {}]", d);
```

**Log Information:**
- **Template Path:** Shows resolved template location
- **Variables:** All context and custom variables
- **Debug Level:** Use `CDK_DEBUG=true` to enable

#### Error Diagnostics
- **Clear Error Messages:** Include full template path
- **Context Information:** Show variable values in logs  
- **Stack Traces:** Point to calling construct
- **Validation:** Fail fast on missing resources

### Extension Points

#### Custom Variable Providers
```java
// Constructs can provide additional context
Template.parse(scope, "template.mustache", Map.of("custom:key", "value"));
```

#### Custom Mustache Functions
Could extend `DefaultMustacheFactory` to add custom functions for:
- Date formatting
- String manipulation  
- Mathematical operations
- Conditional logic

#### Template Inheritance
Could implement template inheritance via:
- Base template loading
- Template composition
- Variable scoping