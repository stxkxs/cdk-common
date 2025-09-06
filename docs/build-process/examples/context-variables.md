# CDK Context Variables

## Default Context Variables

These variables are automatically extracted from CDK context and available in all templates:

### Host Variables

```yaml
host:id: "myapp"
host:organization: "acme"
host:account: "123456789012"
host:region: "us-east-1"
host:name: "my-application"
host:alias: "prod"
host:environment: "production"
host:version: "v1"
host:domain: "example.com"
```

### Hosted Variables

```yaml
hosted:id: "myapp-prod"
hosted:organization: "acme-corp"
hosted:account: "123456789012"
hosted:region: "us-east-1"
hosted:name: "my-service"
hosted:alias: "prod"
hosted:environment: "production"
hosted:version: "v1"
hosted:domain: "api.example.com"
```

### Computed Variables

```yaml
home: "/"
synthesizer:name: "default-synthesizer"
```

## Usage in Templates

### Basic Substitution

```yaml
# Template
name: {{hosted:id}}-bucket
region: {{hosted:region}}

# Processed Result  
name: myapp-prod-bucket
region: us-east-1
```

### Tag Patterns

```yaml
# Template
tags:
  "{{hosted:domain}}:resource-type": "bucket"
  "{{hosted:domain}}:component": "{{hosted:id}}-storage"
  "{{hosted:domain}}:part-of": "{{hosted:organization}}.{{hosted:name}}.{{hosted:alias}}"

# Processed Result
tags:
  "api.example.com:resource-type": "bucket"
  "api.example.com:component": "myapp-prod-storage"  
  "api.example.com:part-of": "acme-corp.my-service.prod"
```

### ARN Construction

```yaml
# Template
roleArn: "arn:aws:iam::{{hosted:account}}:role/{{hosted:id}}-role"
bucketName: "{{hosted:organization}}-{{hosted:name}}-{{hosted:alias}}-data"

# Processed Result
roleArn: "arn:aws:iam::123456789012:role/myapp-prod-role"
bucketName: "acme-corp-my-service-prod-data"
```

## Custom Context Variables

Constructs can provide additional context variables:

```java
// Launch.java example
var parsed = Template.parse(app, "conf.mustache",
  Map.ofEntries(
    Map.entry("hosted:eks:druid:release", "v24.0.0"),
    Map.entry("hosted:tags", tags(app))
  ));
```

### Custom Variable Usage

```yaml
# Template with custom variables
druid:
  image: "apache/druid:{{hosted:eks:druid:release}}"
  
# Processed Result
druid:
  image: "apache/druid:v24.0.0"
```

## Context Variable Sources

1. **CDK App Context** - Set via `cdk.context.json` or CLI
2. **CDK Stack Context** - Passed during stack creation
3. **Construct Context** - Added by individual constructs
4. **Runtime Context** - Computed during template processing