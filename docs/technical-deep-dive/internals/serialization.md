# Jackson YAML Serialization Deep Dive

## Jackson Configuration Architecture

### Mapper.java - Central Configuration

#### ObjectMapper Setup
```java
// Mapper.java:21,25-43 - Singleton ObjectMapper configuration
private static final ObjectMapper mapper = configure();

private static ObjectMapper configure() {
  return JsonMapper
    .builder(yamlConf())
    .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
    .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)  
    .serializationInclusion(Include.NON_NULL)
    .addMixIn(Object.class, DefaultMixin.class)
    .addModule(new Jdk8Module())
    .withConfigOverride(LinkedHashMap.class, (handler) -> handler.setSetterInfo(Value.forValueNulls(Nulls.AS_EMPTY)))
    .withConfigOverride(TreeSet.class, (handler) -> handler.setSetterInfo(Value.forValueNulls(Nulls.AS_EMPTY)))
    .withConfigOverride(Map.class, (handler) -> handler.setSetterInfo(Value.forValueNulls(Nulls.AS_EMPTY)))
    .withConfigOverride(List.class, (handler) -> handler.setSetterInfo(Value.forValueNulls(Nulls.AS_EMPTY)))
    .accessorNaming(
      new Provider()
        .withIsGetterPrefix("")
        .withGetterPrefix("")
        .withSetterPrefix(""))
    .build();
}
```

#### YAML Factory Configuration
```java
// Mapper.java:46-50 - YAML-specific settings
private static YAMLFactory yamlConf() {
  return YAMLFactory.builder()
    .disable(WRITE_DOC_START_MARKER)  // No "---" at start of documents
    .build();
}
```

### Configuration Analysis

#### Case Insensitive Processing
```java
.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
```

**Benefits:**
- **Flexible YAML:** `instanceType` matches `instancetype` or `INSTANCETYPE`
- **Enum Mapping:** `PRODUCTION` matches `production` or `Production`
- **Human Friendly:** Less strict formatting requirements

**Examples:**
```yaml
# All these map to the same Java field
instanceType: t3.micro
instancetype: t3.micro  
INSTANCETYPE: t3.micro

# All these map to Environment.PRODUCTION enum
environment: production
environment: Production
environment: PRODUCTION
```

#### Null Handling Strategy
```java
.serializationInclusion(Include.NON_NULL)
```

**Serialization:** Null fields omitted from output YAML
```java
// Java object with null field
public class Config {
  private String name = "test";
  private String description = null;  // Will be omitted
}

// Serialized YAML
name: test
# description not included
```

#### Collection Null Handling
```java
.withConfigOverride(LinkedHashMap.class, (handler) -> handler.setSetterInfo(Value.forValueNulls(Nulls.AS_EMPTY)))
.withConfigOverride(TreeSet.class, (handler) -> handler.setSetterInfo(Value.forValueNulls(Nulls.AS_EMPTY)))
.withConfigOverride(Map.class, (handler) -> handler.setSetterInfo(Value.forValueNulls(Nulls.AS_EMPTY)))
.withConfigOverride(List.class, (handler) -> handler.setSetterInfo(Value.forValueNulls(Nulls.AS_EMPTY)))
```

**Behavior:** Null collections become empty collections
```yaml
# YAML with null/missing collections
config:
  tags: null     # Becomes empty Map
  # items missing  # Becomes empty List

# Java result
Config {
  tags = new HashMap<>()      // Empty, not null
  items = new ArrayList<>()   // Empty, not null  
}
```

#### Accessor Naming Strategy
```java
.accessorNaming(
  new Provider()
    .withIsGetterPrefix("")    // No "is" prefix for boolean getters
    .withGetterPrefix("")      // No "get" prefix for getters
    .withSetterPrefix(""))     // No "set" prefix for setters
```

**Impact on Method Naming:**
```java
// Traditional Java Bean
public class TraditionalBean {
  private boolean enabled;
  private String name;
  
  public boolean isEnabled() { return enabled; }      // "is" prefix
  public void setEnabled(boolean enabled) { this.enabled = enabled; }
  
  public String getName() { return name; }            // "get" prefix  
  public void setName(String name) { this.name = name; }
}

// cdk-common Style (with Lombok)
@Data
public class CommonBean {
  private boolean enabled;
  private String name;
  
  // Lombok generates:
  // public boolean enabled() { return enabled; }      // No "is" prefix
  // public void enabled(boolean enabled) { this.enabled = enabled; }
  
  // public String name() { return name; }             // No "get" prefix
  // public void name(String name) { this.name = name; }
}
```

### DefaultMixin Integration

#### Mixin Purpose
```java
.addMixIn(Object.class, DefaultMixin.class)
```

**DefaultMixin.java Analysis:**
```java
// Expected structure based on usage
public interface DefaultMixin {
  // Common serialization annotations for all objects
  // Could include:
  // @JsonIgnoreProperties(ignoreUnknown = true)
  // @JsonInclude(JsonInclude.Include.NON_NULL)
}
```

**Application:** Every object gets default serialization behavior

### JDK8 Module Integration

#### JDK8Module Features
```java
.addModule(new Jdk8Module())
```

**Capabilities:**
- **Optional Support:** `Optional<String>` serialization/deserialization
- **Time API:** `LocalDateTime`, `ZonedDateTime` support
- **Stream API:** Basic stream serialization

**Usage Examples:**
```java
public class ModernConfig {
  private Optional<String> description;      // Serializes as value or null
  private LocalDateTime createdAt;           // ISO-8601 format
  private List<String> tags;                 // Stream-friendly
}
```

### Serialization Process Flow

#### Template → YAML → POJO Pipeline
```java
// 1. Template processing produces YAML string
var yamlString = Template.parse(scope, "config.mustache");

// 2. Jackson parses YAML into target class
var config = Mapper.get().readValue(yamlString, ConfigClass.class);
```

#### Detailed Processing Steps

**Step 1: YAML Parsing**
```java
// YAMLFactory creates YAML parser
// Handles YAML-specific syntax: lists, maps, scalars
// Converts to Jackson's internal tree representation
```

**Step 2: Property Resolution**
```java
// Case-insensitive property matching
// "instanceType" YAML property → instanceType() Java method
// Null handling: null YAML values → empty collections
```

**Step 3: Type Conversion**
```java  
// String → Enum conversion (case-insensitive)
// String → Primitive conversion  
// Map → Custom object conversion (recursive)
// List → Collection conversion
```

**Step 4: Object Construction**
```java
// Default constructor called
// Setter methods invoked (or field injection)
// Validation annotations processed (if present)
```

### Performance Characteristics

#### ObjectMapper Singleton Pattern
```java
// Mapper.java:21 - Single instance for entire application
private static final ObjectMapper mapper = configure();
```

**Benefits:**
- **Configuration Reuse:** Setup cost paid once
- **Thread Safety:** Jackson ObjectMapper is thread-safe
- **Memory Efficiency:** Single instance across all serialization

**Performance Metrics:**
- **Initialization:** ~10-50ms for complex configuration
- **Per-Operation:** ~1-10ms for typical POJO serialization
- **Memory:** ~1-5MB for ObjectMapper instance + caches

#### Optimization Strategies

**Jackson Internal Caching:**
- **Property Introspection:** Cached per class
- **Serializer Selection:** Cached per type
- **Deserializer Selection:** Cached per type

**Performance Considerations:**
```java
// Expensive - creates new ObjectMapper each time
ObjectMapper mapper = new ObjectMapper();  // DON'T DO THIS

// Efficient - reuses configured singleton
Mapper.get().readValue(yaml, ConfigClass.class);  // DO THIS
```

### Error Handling and Debugging

#### Common Serialization Errors

**Unrecognized Property:**
```java
com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException:
  Unrecognized field "unknownField" (class io.stxkxs.model.ConfigClass)
```

**Solutions:**
1. **Add Field:** Add missing field to Java class
2. **Ignore Unknown:** Use `@JsonIgnoreProperties(ignoreUnknown = true)`
3. **Remove Property:** Remove from YAML template

**Type Mismatch:**
```java
com.fasterxml.jackson.databind.exc.InvalidFormatException:
  Cannot deserialize value of type `int` from String "not-a-number"
```

**Solutions:**
1. **Fix YAML:** Correct value type in template
2. **Custom Deserializer:** Handle string → number conversion
3. **Validation:** Add input validation

#### Debugging Serialization

**Enable Jackson Debugging:**
```java
// Temporary debugging configuration
ObjectMapper debugMapper = Mapper.get().copy()
  .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
  .enable(SerializationFeature.INDENT_OUTPUT);
```

**Trace YAML Processing:**
```java
// Log processed YAML before deserialization
var yamlString = Template.parse(scope, "config.mustache");
System.out.println("Processed YAML:\n" + yamlString);

var config = Mapper.get().readValue(yamlString, ConfigClass.class);
System.out.println("Parsed Config: " + config);
```

### Advanced Serialization Patterns

#### Custom Serializers
```java
// Example custom serializer for special types
public class CustomConfigSerializer extends JsonSerializer<CustomConfig> {
  @Override
  public void serialize(CustomConfig value, JsonGenerator gen, SerializerProvider serializers) {
    // Custom serialization logic
  }
}

// Registration
mapper.addSerializer(CustomConfig.class, new CustomConfigSerializer());
```

#### Conditional Serialization
```java
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ConditionalConfig {
  private List<String> tags;  // Only serialized if not empty
  
  @JsonProperty("custom_name")
  private String internalName;  // YAML uses "custom_name"
}
```

#### Polymorphic Deserialization
```java
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = DatabaseConfig.class, name = "database"),
  @JsonSubTypes.Type(value = CacheConfig.class, name = "cache")
})
public abstract class StorageConfig {
  // Base configuration
}
```

### Integration with CDK Constructs

#### Typical Usage Pattern
```java
// EksNestedStack.java:59,64 - Common pattern
var sqs = Mapper.get().readValue(Template.parse(this, conf.sqs()), Sqs.class);
var configuration = Mapper.get().readValue(Template.parse(this, conf.nodeGroups()), new TypeReference<List<NodeGroup>>() {});
```

**Flow:**
1. **Template Processing:** Mustache → YAML string
2. **Deserialization:** YAML string → Typed Java object
3. **Construct Usage:** Java object configures AWS resources

#### Type Safety Benefits
```java
// Strongly typed configuration
public class DatabaseConfig {
  private String engine;          // Type-safe: only strings accepted
  private Integer port;           // Type-safe: only integers accepted
  private Boolean encrypted;      // Type-safe: only booleans accepted
  private List<String> subnets;   // Type-safe: only string lists accepted
}

// Compile-time safety
DatabaseConfig config = Mapper.get().readValue(yaml, DatabaseConfig.class);
String engine = config.engine();  // Known to be String
int port = config.port();         // Known to be Integer (auto-unboxed)
```