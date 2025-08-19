# ADR-002: Mustache Template Engine Choice

## Status
Accepted

## Context
The cdk-common library needs a templating engine to process YAML configuration files with dynamic values from CDK context. The engine must support variable substitution, be lightweight, and integrate well with Java ecosystems.

## Decision
We will use Mustache (mustache.java library) for template processing.

**Key Implementation:**
```java
// Template.java:98-100
factory.compile(new InputStreamReader(stream, StandardCharsets.UTF_8), template)
  .execute(writer, values)
  .flush();
```

**Template Syntax:**
```yaml
name: {{hosted:id}}-vpc-cni
account: {{hosted:account}}
tags:
  "{{hosted:domain}}:component": {{hosted:id}}-component
```

## Rationale

### Why Mustache?

#### 1. Logic-Less Templates
- **Principle:** Separation of logic and presentation
- **Benefit:** Templates remain readable and maintainable
- **Example:** No complex conditionals cluttering YAML structure

#### 2. Simple Syntax
- **Variables:** `{{variable}}`
- **Sections:** `{{#section}}...{{/section}}`
- **Comments:** `{{! comment }}`
- **Minimal learning curve for teams**

#### 3. Mature Java Integration
- **Library:** `com.github.spullara.mustache.java:compiler`
- **Performance:** Compiled templates with good performance
- **Stability:** Well-established library with long track record

#### 4. YAML Compatibility  
- **No Syntax Conflicts:** Mustache syntax doesn't interfere with YAML
- **Preserves Structure:** YAML remains valid after processing
- **Tool Support:** YAML editors/validators work with templates

### Template Processing Flow

```java
// 1. Load template from classpath
var stream = Template.class.getClassLoader().getResourceAsStream(template);

// 2. Create Mustache factory and compile template
var factory = new DefaultMustacheFactory();
var mustache = factory.compile(new InputStreamReader(stream), template);

// 3. Execute with context variables
mustache.execute(writer, contextVariables);

// 4. Parse result as YAML into POJO
var result = Mapper.get().readValue(processedYaml, TargetClass.class);
```

## Consequences

### Positive
- **Readable Templates:** YAML structure preserved, easy to understand
- **Type Safety:** Templates produce valid YAML for strong typing
- **Performance:** Compiled templates execute efficiently
- **Maintainability:** Logic-less design prevents template complexity
- **Ecosystem:** Good Java tooling and IDE support

### Negative
- **Limited Logic:** Complex conditionals require Java-side processing
- **Learning Curve:** Teams need to learn Mustache syntax
- **Debugging:** Template processing errors can be hard to trace

### Alternatives Considered

#### Freemarker
```yaml
name: ${hosted.id}-vpc-cni
<#if production>
  retainOnDelete: true
</#if>
```
**Rejected:** Too powerful, encourages logic in templates, complex syntax

#### Velocity
```yaml
name: ${hosted.id}-vpc-cni
#if($production)
  retainOnDelete: true
#end
```
**Rejected:** Legacy technology, complex setup, security concerns

#### String.format()
```java
String.format("name: %s-vpc-cni", hostedId)
```
**Rejected:** No template files, poor maintainability, mixing code/config

#### Thymeleaf
```yaml
name: "[(${hosted.id})]-vpc-cni"
```
**Rejected:** Primarily web-focused, overkill for YAML processing

## Implementation Notes

### Variable Context
```java
// Template.java:71-88 - Default variables
Map.entry("host:id", scope.getNode().getContext("host:id").toString())
Map.entry("hosted:account", scope.getNode().getContext("hosted:account").toString())
```

### Custom Variables
```java
// Launch.java:49-52 - Additional context
Template.parse(app, "conf.mustache", 
  Map.ofEntries(
    Map.entry("hosted:eks:druid:release", "v24.0.0"),
    Map.entry("hosted:tags", tags(app))
  ));
```

### Error Handling
- Missing variables: Mustache renders empty string
- Missing templates: RuntimeException with path details
- Invalid YAML: Jackson parsing exceptions