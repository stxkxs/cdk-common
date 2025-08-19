# Troubleshooting Guide

## Common Issues and Solutions

### Template Resolution Issues

#### Template Not Found
**Error:**
```
error parsing template! can not find prototype/v1/eks/addons.mustache.
```

**Causes & Solutions:**

1. **Incorrect Path Structure**
   ```bash
   # Check your directory structure
   ls -la src/main/resources/prototype/v1/
   
   # Should match: {environment}/{version}/{template-file}
   ```

2. **Wrong Environment/Version Context**
   ```json
   // Check cdk.context.json
   {
     "host:environment": "prototype",  // Must match directory name
     "host:version": "v1"             // Must match directory name
   }
   ```

3. **Missing Template File**
   ```bash
   # Create missing template
   mkdir -p src/main/resources/prototype/v1/eks
   touch src/main/resources/prototype/v1/eks/addons.mustache
   ```

#### Template Path Debugging
```java
// Add debugging to see resolved paths
System.out.println("Environment: " + scope.getNode().getContext("host:environment"));
System.out.println("Version: " + scope.getNode().getContext("host:version"));
System.out.println("Template: " + templateFile);
```

### Context Variable Issues

#### Missing Context Variables
**Error:**
```
java.lang.NullPointerException: Cannot invoke "toString()" because 
the return value of "software.constructs.Node.getContext(String)" is null
```

**Solutions:**

1. **Add Missing Context**
   ```json
   // cdk.context.json
   {
     "host:id": "myapp",
     "host:account": "123456789012",
     "hosted:organization": "acme"
   }
   ```

2. **Use tryGetContext() for Optional Values**
   ```java
   // Instead of getContext()
   var optional = scope.getNode().tryGetContext("optional:value");
   var value = optional != null ? optional.toString() : "default";
   ```

3. **Check Context Inheritance**
   ```java
   // Context flows from parent to child constructs
   // Make sure context is set at App or Stack level
   app.getNode().setContext("host:id", "myapp");
   ```

#### Context Variable Naming
**Common Mistakes:**
```json
// Wrong - inconsistent naming
{
  "hostId": "myapp",           // Should be "host:id"
  "hosted_account": "123"      // Should be "hosted:account"  
}

// Correct - use colon-separated namespaces
{
  "host:id": "myapp",
  "hosted:account": "123456789012"
}
```

### Template Processing Issues

#### Mustache Syntax Errors
**Error:**
```
com.github.mustachejava.MustacheException: 
  Unknown tag: hosted:id at line 5
```

**Solutions:**

1. **Check Variable Syntax**
   ```yaml
   # Wrong
   name: {hosted:id}-bucket      # Missing second brace
   name: {{hosted:id}-bucket     # Missing closing brace
   
   # Correct  
   name: {{hosted:id}}-bucket
   ```

2. **Escape Special Characters**
   ```yaml
   # If you need literal braces
   name: "{{hosted:id}}-bucket\{\{literal\}\}"
   ```

#### Variable Not Substituted
**Symptom:** Template variables remain as `{{variable}}` in output

**Causes:**
1. **Variable not in context**
   ```java
   // Add missing variable to context
   Template.parse(scope, "template.mustache", 
     Map.of("missing:variable", "value"));
   ```

2. **Typo in variable name**
   ```yaml
   # Template uses: {{hosted:id}}
   # Context has: "hostedId" 
   # Fix: Use "hosted:id" in context
   ```

### YAML Parsing Issues

#### Invalid YAML Structure
**Error:**
```
com.fasterxml.jackson.dataformat.yaml.snakeyaml.error.YAMLException:
  mapping values are not allowed here
```

**Solutions:**

1. **Check YAML Indentation**
   ```yaml
   # Wrong - mixing tabs and spaces
   config:
   	name: value    # Tab here
       id: value      # Spaces here
   
   # Correct - consistent spaces
   config:
     name: value
     id: value
   ```

2. **Validate YAML Syntax**
   ```bash
   # Use yamllint or online validators
   yamllint src/main/resources/prototype/v1/config.mustache
   ```

#### Unrecognized Properties
**Error:**
```
com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException:
  Unrecognized field "invalidField" (class io.stxkxs.model.RdsConf)
```

**Solutions:**

1. **Check POJO Field Names**
   ```java
   // Make sure YAML properties match Java fields
   public class RdsConf {
     private String engine;        // YAML: engine
     private String engineVersion; // YAML: engineVersion or engine_version
   }
   ```

2. **Use Jackson Annotations**
   ```java
   @JsonProperty("custom_name")
   private String customField;
   
   @JsonIgnoreProperties(ignoreUnknown = true)
   public class FlexibleConf { }
   ```

### Build and Deployment Issues

#### CDK Synthesis Fails
**Error:**
```
Error: spawn java ENOENT
```

**Solutions:**

1. **Check Java Installation**
   ```bash
   java -version
   # Should show Java 21+
   ```

2. **Verify Maven Build**
   ```bash
   mvn clean compile
   mvn package
   ```

3. **Check CDK Version Compatibility**
   ```bash
   cdk --version
   # Should be compatible with library CDK version
   ```

#### Resource Name Conflicts
**Error:**
```
Resource with id 'bucket' already exists
```

**Solutions:**

1. **Check Template Variable Uniqueness**
   ```yaml
   # Ensure variables create unique names
   bucketName: "{{hosted:organization}}-{{hosted:name}}-{{hosted:alias}}-data"
   ```

2. **Use Proper Scoping**
   ```java
   // Use unique construct IDs
   new S3Construct(this, "data-bucket", config);
   new S3Construct(this, "logs-bucket", config);
   ```

## Debugging Strategies

### 1. Enable Debug Logging
```bash
export CDK_DEBUG=true
cdk synth
```

### 2. Print Template Processing
```java
// Add temporary debug output
var processed = Template.parse(scope, "template.mustache");
System.out.println("Processed template: " + processed);
var config = Mapper.get().readValue(processed, ConfigClass.class);
```

### 3. Validate Templates Independently
```java
// Test template processing without CDK
public static void main(String[] args) {
  var factory = new DefaultMustacheFactory();
  var template = factory.compile("test.mustache");
  var output = template.execute(new StringWriter(), testVariables).toString();
  System.out.println(output);
}
```

### 4. Check Generated CloudFormation
```bash
# Examine CDK output
cat cdk.out/MyStack.template.json | jq '.Resources'
```

## Getting Help

### 1. Check Template Resolution
```java
// Template.java:38 - Enable debug logging to see:
// "parsing template prototype/v1/eks/addons.mustache with parameters {host:id=myapp, ...}"
```

### 2. Validate Context Variables
```java
// Print all available context
scope.getNode().getAllContext().forEach((k, v) -> 
  System.out.println(k + " = " + v));
```

### 3. Test Template Syntax
Use online Mustache testers with your template content and variables.

### 4. Maven Dependency Issues
```bash
# Check for conflicting versions
mvn dependency:tree | grep -E "(jackson|mustache|aws-cdk)"
```