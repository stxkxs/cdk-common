# CDK Context System Deep Dive

## Context Architecture

### CDK Context Fundamentals

#### Context Tree Structure
```java
// CDK context flows through construct tree hierarchy
App
├── Stack
│   ├── Construct A (inherits app + stack context)
│   └── Construct B (inherits app + stack context)
└── Stack 2
    └── Construct C (inherits app + stack 2 context)
```

**Context Inheritance Rules:**
1. **Hierarchical:** Child constructs inherit parent context
2. **Additive:** Child context supplements parent context
3. **Override:** Child values override parent values with same key
4. **Immutable:** Context cannot be modified after set

#### Context Storage Implementation
```java
// CDK Node.java (conceptual)
public class Node {
  private final Map<String, Object> context = new HashMap<>();
  
  public Object getContext(String key) {
    // Search up the tree for context value
    Object value = this.context.get(key);
    if (value != null) return value;
    
    if (parent != null) {
      return parent.getNode().getContext(key);
    }
    
    throw new RuntimeException("Context value not found: " + key);
  }
}
```

### cdk-common Context Usage

#### Context Variable Categories

**Host Variables (Physical Infrastructure):**
```java
// Template.java:71-78 - Host context extraction
Map.entry("host:id", scope.getNode().getContext("host:id").toString()),
Map.entry("host:organization", scope.getNode().getContext("host:organization").toString()),
Map.entry("host:account", scope.getNode().getContext("host:account").toString()),
Map.entry("host:region", scope.getNode().getContext("host:region").toString()),
Map.entry("host:name", scope.getNode().getContext("host:name").toString()),
Map.entry("host:alias", scope.getNode().getContext("host:alias").toString()),
Map.entry("host:environment", scope.getNode().getContext("host:environment").toString()),
Map.entry("host:version", scope.getNode().getContext("host:version").toString()),
```

**Hosted Variables (Logical Services):**
```java
// Template.java:79-88 - Hosted context extraction  
Map.entry("hosted:id", scope.getNode().getContext("hosted:id").toString()),
Map.entry("hosted:organization", scope.getNode().getContext("hosted:organization").toString()),
Map.entry("hosted:account", scope.getNode().getContext("hosted:account").toString()),
Map.entry("hosted:region", scope.getNode().getContext("hosted:region").toString()),
Map.entry("hosted:name", scope.getNode().getContext("hosted:name").toString()),
Map.entry("hosted:alias", scope.getNode().getContext("hosted:alias").toString()),
Map.entry("hosted:environment", scope.getNode().getContext("hosted:environment").toString()),
Map.entry("hosted:version", scope.getNode().getContext("hosted:version").toString()),
```

**Computed Variables:**
```java
// Template.java:58-66 - Computed context values
var home = Optional.of(scope)
  .map(s -> s.getNode().tryGetContext("home"))
  .map(Object::toString)
  .orElse("/");

var synthesizer = Optional.of(scope)
  .map(s -> s.getNode().tryGetContext("hosted:synthesizer:name"))
  .map(Object::toString)
  .orElseGet(Common::id_);
```

#### Context Variable Resolution Strategy

**Required vs Optional Context:**
```java
// Required - throws NPE if missing
scope.getContext("host:id")

// Optional - returns null if missing
scope.tryGetContext("optional:value")
```

**Type Handling:**
```java
// All context values converted to strings for template processing
.toString()
```

**Error Handling:**
```java
// NPE thrown for missing required context
// Construct should fail fast during synthesis
```

### Context Sources and Precedence

#### 1. cdk.context.json (Project Level)
```json
{
  "host:environment": "prototype",
  "host:version": "v1",
  "host:account": "123456789012"
}
```

**Characteristics:**
- **Lowest Priority:** Overridden by other sources
- **Version Controlled:** Committed to repository
- **Team Defaults:** Shared across team members

#### 2. CLI Arguments (Deployment Time)
```bash
cdk deploy --context host:environment=production --context host:account=987654321098
```

**Characteristics:**
- **High Priority:** Overrides cdk.context.json
- **Deployment Specific:** Different per deployment
- **Runtime Flexibility:** Change without code modification

#### 3. App Code Context (Application Level)
```java
// Launch.java equivalent
public static void main(String[] args) {
  var app = new App();
  
  // Set app-level context
  app.getNode().setContext("host:id", "myapp");
  app.getNode().setContext("host:organization", "acme");
  
  new MyStack(app, stackProps);
  app.synth();
}
```

**Characteristics:**
- **Medium Priority:** Overrides file, overridden by CLI
- **Code-Driven:** Managed in application code
- **Dynamic:** Can be computed at runtime

#### 4. Stack Context (Stack Level)
```java
public class MyStack extends Stack {
  public MyStack(Scope scope, StackProps props) {
    super(scope, "MyStack", props);
    
    // Stack-specific context
    this.getNode().setContext("stack:specific", "value");
  }
}
```

**Characteristics:**
- **Stack Scoped:** Only available to constructs in this stack
- **Override Capability:** Can override app-level context
- **Construct Specific:** Tailored to stack needs

#### 5. Custom Context (Construct Level)
```java
// Launch.java:49-52 - Custom context per template
Template.parse(app, "conf.mustache",
  Map.ofEntries(
    Map.entry("hosted:eks:druid:release", "v24.0.0"),
    Map.entry("hosted:tags", tags(app))
  ));
```

**Characteristics:**
- **Highest Priority:** Overrides all other context
- **Template Specific:** Only for specific template processing
- **Dynamic Values:** Can be computed at synthesis time

### Context Resolution Algorithm

#### Variable Lookup Process
```java
// Template.java:33 - Variable merging
Maps.from(defaults(scope), values)
```

**Resolution Order:**
1. **Custom Variables** (highest priority)
2. **Default Context Variables** 
3. **CDK Context Hierarchy** (app → stack → construct)
4. **cdk.context.json** (lowest priority)

#### Variable Merging Implementation
```java
// Common.Maps.from conceptual implementation
public static Map<String, Object> from(Map<String, Object> defaults, Map<String, Object> overrides) {
  var result = new HashMap<>(defaults);
  result.putAll(overrides);  // Overrides take precedence
  return result;
}
```

### Context Performance Characteristics

#### Context Access Performance
```java
// Context lookup complexity: O(depth) where depth = construct tree depth
// Typical depth: 3-5 levels (App → Stack → Construct → SubConstruct)
// Access time: ~1-5 microseconds per lookup
```

#### Memory Usage
- **Context Storage:** HashMap per construct node
- **String Values:** All context values stored as objects
- **Tree References:** Parent/child references maintained
- **Garbage Collection:** Context cleaned up with construct tree

#### Optimization Strategies
1. **Context Caching:** Cache frequently accessed values
2. **Batch Extraction:** Extract all needed context at once
3. **Type Conversion:** Convert types once, reuse
4. **Scope Limiting:** Set context at appropriate scope level

### Advanced Context Patterns

#### Environment-Specific Context
```json
// Different context files per environment
// cdk.context.prototype.json
{
  "host:environment": "prototype",
  "host:account": "123456789012"
}

// cdk.context.production.json  
{
  "host:environment": "production",
  "host:account": "987654321098"
}
```

#### Context Validation
```java
// Add validation in constructs
public class ValidatedConstruct extends Construct {
  public ValidatedConstruct(Scope scope, String id) {
    super(scope, id);
    
    validateContext();
  }
  
  private void validateContext() {
    var environment = this.getNode().getContext("host:environment");
    if (!Arrays.asList("prototype", "production").contains(environment)) {
      throw new IllegalArgumentException("Invalid environment: " + environment);
    }
  }
}
```

#### Dynamic Context Generation
```java
// Launch.java:57-75 - Runtime context computation
private static ArrayList<Map<String, String>> tags(App app) {
  var tags = app.getNode().getContext("hosted:tags");
  var results = new ArrayList<Map<String, String>>();
  
  if (tags instanceof List<?> tagList) {
    for (var tag : tagList) {
      if (tag instanceof Map<?, ?> tagMap) {
        // Process and validate tag structure
        var safeTagMap = new HashMap<String, String>();
        for (var entry : tagMap.entrySet()) {
          if (entry.getKey() instanceof String key && entry.getValue() instanceof String value) {
            safeTagMap.put(key, value);
          }
        }
        results.add(safeTagMap);
      }
    }
  }
  
  return results;
}
```

### Debugging Context Issues

#### Context Inspection
```java
// Debug all available context
public void debugContext(Construct scope) {
  scope.getNode().getAllContext().forEach((key, value) -> {
    System.out.println(key + " = " + value + " (" + value.getClass().getSimpleName() + ")");
  });
}
```

#### Context Tracing
```java
// Trace context resolution
public Object traceGetContext(Construct scope, String key) {
  var current = scope;
  while (current != null) {
    var value = current.getNode().tryGetContext(key);
    if (value != null) {
      System.out.println("Found " + key + " = " + value + " at " + current.getNode().getId());
      return value;
    }
    current = current.getNode().getScope();
  }
  System.out.println("Context not found: " + key);
  return null;
}
```

#### Context Validation Tools
```java
// Validate required context exists
public static void validateRequiredContext(Construct scope, String... requiredKeys) {
  for (String key : requiredKeys) {
    var value = scope.getNode().tryGetContext(key);
    if (value == null) {
      throw new IllegalStateException("Required context missing: " + key);
    }
  }
}
```