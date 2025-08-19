# ADR-001: Environment/Version Template Path Structure

## Status
Accepted

## Context
The cdk-common library needs a strategy for organizing and resolving configuration templates across different deployment environments and API versions. Teams need to maintain different configurations for development, staging, and production while supporting backwards compatibility.

## Decision
We will use a hierarchical path structure: `{environment}/{version}/{template-file}` for template resolution.

**Environment Categories:**
- `bootstrap` - Initial infrastructure setup
- `prototype` - Development and testing configurations  
- `production` - Production-ready configurations

**Version Strategy:**
- `v1`, `v2`, `v3` - Semantic versioning for template schemas
- Allows backwards compatibility during library upgrades
- Enables gradual migration of configurations

## Rationale

### Why Environment-Based Separation?
1. **Configuration Isolation** - Development configs often have relaxed security, debugging enabled
2. **Resource Naming** - Different naming conventions across environments
3. **Scaling Differences** - Production requires different capacity planning
4. **Security Policies** - Production demands stricter IAM policies

### Why Version-Based Separation?
1. **Backwards Compatibility** - Teams can upgrade library without breaking configs
2. **Gradual Migration** - Move templates to new versions incrementally  
3. **Schema Evolution** - Template structures can evolve over time
4. **Rollback Safety** - Fall back to previous versions if needed

### Implementation Benefits
```java
// Template.java:42-43 - Simple path construction
var prefix = String.format("%s/%s", environment, version);
var template = String.format("%s/%s", prefix, file);
```

**Path Examples:**
- `prototype/v1/eks/addons.mustache` - Development EKS config
- `production/v1/eks/addons.mustache` - Production EKS config
- `production/v2/eks/addons.mustache` - New schema version

## Consequences

### Positive
- Clear separation of concerns
- Predictable template resolution
- Supports multiple environments
- Enables schema versioning
- ClassLoader integration works seamlessly

### Negative
- Potential template duplication across environments
- Version management overhead
- Directory structure complexity

### Alternatives Considered

#### Single Flat Directory
```
templates/
├── eks-addons-dev.mustache
├── eks-addons-prod.mustache
└── policy-karpenter-v1.mustache
```
**Rejected:** Naming becomes unwieldy, no clear organization

#### Environment Variables Only
Using environment variables for template selection without path structure.
**Rejected:** Less predictable, harder to organize, no version support

#### Git Branch Strategy  
Different branches for different environments.
**Rejected:** Complicates deployment, version management becomes difficult

## Implementation Notes
- Templates loaded via ClassLoader from `src/main/resources/{environment}/{version}/`
- Missing templates throw runtime exceptions with full path details
- Environment/version extracted from CDK context during build
- Consumer projects provide templates matching their deployment needs