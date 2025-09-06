# Template Structure Examples

## Directory Organization

### Complete Template Hierarchy

```
src/main/resources/
├── prototype/
│   └── v1/
│       ├── conf.mustache              # Main configuration
│       ├── api/
│       │   └── user.mustache          # API Gateway configs
│       ├── auth/
│       │   ├── ses.mustache           # SES email configuration
│       │   ├── sns.mustache           # SNS SMS configuration
│       │   ├── triggers.mustache      # Cognito triggers
│       │   ├── userpool.mustache      # User pool configuration
│       │   └── userpoolclient.mustache
│       ├── eks/
│       │   ├── addons.mustache        # EKS add-ons
│       │   ├── node-groups.mustache   # Worker node configs
│       │   ├── observability.mustache # Monitoring setup
│       │   ├── rbac.mustache          # RBAC configurations
│       │   ├── sqs.mustache           # SQS for interruption
│       │   ├── storage-class.yaml     # Static YAML
│       │   └── tenancy.mustache       # Multi-tenancy
│       ├── helm/
│       │   ├── alloy-operator.mustache
│       │   ├── aws-load-balancer.mustache
│       │   ├── cert-manager.mustache
│       │   ├── grafana.mustache
│       │   └── karpenter.mustache
│       └── policy/
│           ├── aws-load-balancer-controller.mustache
│           ├── bucket-access.mustache
│           ├── karpenter.mustache
│           └── secret-access.mustache
└── production/
    └── v1/
        └── [similar structure with production configs]
```

## Template Categories

### 1. Main Configuration Templates

```yaml
# conf.mustache - Root configuration
host:
  common:
    id: "{{host:id}}"
    name: "{{host:name}}"
    account: "{{host:account}}"
    region: "{{host:region}}"

hosted:
  common:
    id: "{{hosted:id}}"
    organization: "{{hosted:organization}}"
    
deployment:
  eks: eks/addons.mustache
  auth: auth/userpool.mustache
```

### 2. Service-Specific Templates

```yaml
# eks/addons.mustache - EKS add-on configuration
managed:
  awsVpcCni:
    name: vpc-cni
    version: v1.19.6-eksbuild.1
    serviceAccount:
      role:
        name: {{hosted:id}}-vpc-cni
        managedPolicyNames:
          - AmazonEKS_CNI_Policy

  awsEbsCsi:
    name: aws-ebs-csi-driver
    defaultStorageClass: eks/storage-class.yaml
    customPolicies:
      - name: {{hosted:id}}-eks-ebs-encryption
        policy: policy/kms-eks-ebs-encryption.mustache
        mappings:
          account: {{hosted:account}}
          alias: alias/{{hosted:id}}-eks-ebs-encryption
```

### 3. Policy Templates

```yaml
# policy/karpenter.mustache - IAM policy for Karpenter
- Effect: Allow
  Action:
    - ec2:CreateFleet
    - ec2:CreateTags
    - ec2:DescribeInstances
  Resource: "*"
  
- Effect: Allow
  Action:
    - sqs:DeleteMessage
    - sqs:GetQueueAttributes
    - sqs:ReceiveMessage
  Resource: "arn:aws:sqs:{{hosted:region}}:{{hosted:account}}:{{queue}}"
```

### 4. Helm Values Templates

```yaml
# helm/karpenter.mustache - Karpenter Helm values
clusterName: {{hosted:id}}-eks
clusterEndpoint: {{cluster:endpoint}}

serviceAccount:
  name: {{hosted:id}}-karpenter-sa
  
nodeClassRef:
  apiVersion: karpenter.k8s.aws/v1beta1
  kind: EC2NodeClass
  name: {{hosted:id}}-node-class
```

## Template Composition Patterns

### 1. Nested Template References

```yaml
# Parent template references child templates
addons: eks/addons.mustache
policies: 
  - policy/karpenter.mustache
  - policy/aws-load-balancer-controller.mustache
```

### 2. Conditional Content

```yaml
# Using Mustache conditionals
{{#eks.enabled}}
cluster:
  name: {{hosted:id}}-eks
  addons: eks/addons.mustache
{{/eks.enabled}}
```

### 3. Repeated Sections

```yaml
# Iterating over collections
{{#nodeGroups}}
- name: {{name}}
  instanceType: {{instanceType}}
  scalingConfig:
    minSize: {{minSize}}
    maxSize: {{maxSize}}
{{/nodeGroups}}
```

## Static vs Dynamic Templates

### Static Templates

```yaml
# eks/storage-class.yaml - No Mustache processing
apiVersion: storage.k8s.io/v1
kind: StorageClass
metadata:
  name: gp3-encrypted
provisioner: ebs.csi.aws.com
parameters:
  type: gp3
  encrypted: "true"
```

### Dynamic Templates

```yaml
# eks/addons.mustache - Mustache processing
apiVersion: storage.k8s.io/v1
kind: StorageClass
metadata:
  name: {{hosted:id}}-gp3-encrypted
  labels:
    "{{hosted:domain}}/component": "{{hosted:id}}-storage"
provisioner: ebs.csi.aws.com
parameters:
  type: gp3
  encrypted: "true"
  kmsKeyId: "alias/{{hosted:id}}-eks-ebs-encryption"
```