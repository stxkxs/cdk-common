# Template System

## Template Resolution Strategy

The cdk-common library uses a hierarchical template resolution system based on environment and version.

### Path Convention

```
src/main/resources/{environment}/{version}/{template-file}
```

**Environments:**
- `bootstrap` - Initial setup templates
- `prototype` - Development/testing templates  
- `production` - Production-ready templates

**Versions:**
- `v1`, `v2`, `v3` - API/template versions for backwards compatibility

### Template Loading Mechanism

Templates are loaded via the Java ClassLoader:

```java
// Template.java:45
var stream = Template.class.getClassLoader().getResourceAsStream(template);
```

This allows templates to be:
- Packaged in JARs
- Loaded from classpath
- Overridden by consumers

### Mustache Processing

Templates use Mustache syntax for variable substitution:

```yaml
# Template example
name: {{hosted:id}}-vpc-cni
account: {{hosted:account}}
tags:
  "{{hosted:domain}}:component": {{hosted:id}}-component
```

**Variable Sources:**
1. **Default Context** - Extracted from CDK context (Template.java:71-88)
2. **Custom Variables** - Passed per-construct via mappings
3. **Computed Values** - Generated during processing

### Template Organization Patterns

**Service-Specific Templates:**
```
eks/
├── addons.mustache       # EKS add-on configurations
├── node-groups.mustache  # Node group definitions
└── rbac.mustache         # RBAC configurations
```

**Policy Templates:**
```
policy/
├── karpenter.mustache           # Karpenter permissions
├── aws-load-balancer.mustache   # ALB controller permissions
└── secret-access.mustache       # Secrets Manager access
```

**Nested Template References:**
```yaml
# addons.mustache
managed:
  awsEbsCsi:
    defaultStorageClass: eks/storage-class.yaml  # References another template
    customPolicies:
      - policy: policy/kms-eks-ebs-encryption.mustache  # Policy template
```

## Error Handling

**Missing Template:**
```java
if (stream == null) {
  var m = String.format("error parsing template! can not find %s.", template);
  throw new RuntimeException(m);
}
```

**Template Path Examples:**
- Found: `prototype/v1/eks/addons.mustache`
- Not Found: `production/v2/missing-template.mustache` → Exception

## Template Caching

Templates are processed on-demand during construct creation. No explicit caching is implemented - relies on ClassLoader efficiency.