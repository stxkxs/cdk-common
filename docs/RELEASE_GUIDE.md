# Professional Release Guide

## 🚀 Quick Start

To create a professional release:

1. Go to [Actions → Publish Release](../../actions/workflows/publish-release.yml)
2. Click "Run workflow"
3. Fill in the release information
4. Review the draft (if created as draft)
5. Publish when ready

## 📝 Release Inputs Explained

### Version (Required)

- **Format**: `X.Y.Z` or `X.Y.Z-suffix`
- **Examples**:
    - `1.0.0` - First stable release
    - `2.1.0` - Feature release
    - `2.1.1` - Patch/bugfix release
    - `3.0.0-beta1` - Beta pre-release
    - `3.0.0-rc1` - Release candidate

### Release Title (Optional)

- **Default**: "Release vX.Y.Z"
- **Better Examples**:
    - "🎉 Initial Release - Core CDK Components"
    - "✨ Spring Release - Enhanced EKS Support"
    - "🐛 Patch Release - Critical Security Fixes"
    - "🚀 Major Release - Breaking Changes & New Architecture"

### Release Notes (Optional but Recommended)

The most important part! Use the template or write custom notes:

```markdown
## What's Changed

### 🎯 Highlights
- Major feature or improvement that users care about
- Another significant change

### ✨ New Features
- Added support for AWS EKS 1.28
- New MskConstruct for serverless Kafka clusters
- Enhanced VPC configuration options

### 🐛 Bug Fixes
- Fixed memory leak in RestApiConstruct (#123)
- Resolved dependency conflicts with Jackson 2.19
- Corrected IAM role permissions for synthesizer

### 💥 Breaking Changes
- Renamed `Common.Maps` to `Common.MapUtils`
- Changed VpcConf constructor signature
- Minimum Java version 1.0.0now 21 (was 17)

### 📦 Dependencies
- Updated AWS CDK to 2.199.0
- Updated AWS SDK to 2.32.31
- Bumped Jackson to 2.19.2 for security fixes

### 🔧 Internal Changes
- Improved build performance by 30%
- Added comprehensive SpotBugs analysis
- Enhanced test coverage to 85%

### 📚 Documentation
- Added Checkstyle IDE setup guide
- Created comprehensive usage examples
- Updated API documentation

### 🙏 Acknowledgments
Special thanks to @contributor for their work on the EKS module!
```

### Pre-release Flag

- Check this for beta, RC, or experimental releases
- Pre-releases won't be marked as "latest"
- Users must explicitly opt-in to use them

### Draft Flag

- **Recommended for first-time releases!**
- Creates the release as a draft for review
- You can edit before publishing
- Great for team review process

## 📋 Pre-Release Checklist

Before creating a release, ensure:

```bash
# 1. All tests pass
mvn clean test

# 2. Code quality checks pass
mvn checkstyle:check
mvn pmd:check pmd:cpd-check
mvn spotbugs:check

# 3. Dependencies are up to date
mvn versions:display-dependency-updates

# 4. Documentation is current
- README.md reflects new features
- USAGE.md has updated examples
- Javadocs are complete

# 5. Version number is correct
- Check pom.xml version
- Follows semantic versioning
```

## 🎯 Release Strategy

### Version Numbering (Semantic Versioning)

```
MAJOR.MINOR.PATCH-PRERELEASE

1.2.3-beta1
│ │ │   │
│ │ │   └─── Pre-release identifier (optional)
│ │ └─────── Patch: Bug fixes, minor updates
│ └───────── Minor: New features, backwards compatible
└─────────── Major: Breaking changes
```

### When to Release

#### Patch Release (1.0.X)

- Bug fixes
- Security updates
- Documentation fixes
- Dependency updates (non-breaking)

#### Minor Release (1.X.0)

- New features
- New constructs
- Performance improvements
- Backwards-compatible changes

#### Major Release (X.0.0)

- Breaking API changes
- Major architectural changes
- Removal of deprecated features
- Significant behavior changes

## 🏷️ Writing Professional Release Notes

### DO ✅

- **Start with impact**: What does this mean for users?
- **Use clear categories**: Features, Fixes, Breaking Changes
- **Include issue/PR numbers**: "Fixed XYZ (#123)"
- **Credit contributors**: "@username" mentions
- **Provide migration guides**: For breaking changes
- **Use emojis sparingly**: For visual organization
- **Include examples**: Show, don't just tell

### DON'T ❌

- Don't use internal jargon
- Don't forget breaking changes
- Don't be too technical for users
- Don't forget to test the release
- Don't rush - use draft mode

## 📊 Release Templates

### Feature Release Template

```markdown
## 🎉 New Features Release

This release introduces powerful new capabilities for AWS CDK infrastructure management.

### What's New
- **Headline Feature**: Brief description of the main feature
- **Secondary Feature**: Another important addition

### Improvements
- Performance: 40% faster stack synthesis
- Developer Experience: New debugging utilities
- Documentation: Comprehensive guides added

### Coming Next
We're working on X, Y, Z for the next release.

[Full Changelog](link-to-comparison)
```

### Security Release Template

```markdown
## 🔒 Security Update

This release addresses security vulnerabilities and updates dependencies.

### Security Fixes
- **CVE-2024-XXXX**: Updated Jackson to patch vulnerability
- **Dependency Updates**: All dependencies updated to latest secure versions

### Recommended Action
All users should upgrade to this version 1.0.0immediately.

### Details
[Security Advisory](link-to-advisory)
```

### Major Release Template

```markdown
## 🚀 Major Release - Version X.0.0

A significant milestone with new architecture and capabilities.

### Migration Required
Users upgrading from version 1.0.0Y will need to make changes.
See our [Migration Guide](link-to-guide).

### Highlights
- Complete redesign of X component
- New Y feature set
- Performance improvements across the board

### Breaking Changes
- [List all breaking changes with migration paths]

### Deprecations
- X is deprecated, will be removed in version 1.0.0X+1
```

## 🔄 Post-Release Activities

After publishing a release:

1. **Verify artifacts**: Download and test the JAR
2. **Update dependent projects**: If you have other repos using this
3. **Announce the release**:
    - Team Slack/Discord
    - Twitter/LinkedIn (for major releases)
    - Blog post (for significant features)
4. **Monitor issues**: Watch for user feedback
5. **Update roadmap**: Mark completed items

## 🆘 Troubleshooting

### Release Failed to Build

```bash
# Check locally first
mvn clean package
mvn test
```

### Wrong Version Number

- Delete the release and tag from GitHub
- Run workflow again with correct version

### Missing Release Notes

- Edit the release on GitHub after creation
- Or recreate as draft first

### Tag Already Exists

```bash
# Delete local and remote tag
git tag -d v1.0.0
git push origin :refs/tags/v1.0.0
```

## 📚 Examples of Great Release Notes

### Example 1: Feature-Rich Release

```markdown
## Release v2.5.0 - Enhanced Observability 🔍

### Why This Release Matters
This release makes it easier to monitor and debug your CDK applications with comprehensive observability features.

### ✨ New Features
- **CloudWatch Dashboard Generator**: Automatically create dashboards for all constructs
  ```java
  new DashboardConstruct(this, common, dashboardConf);
  ```

- **X-Ray Tracing**: Built-in distributed tracing support
- **Custom Metrics**: Easy metric creation for business KPIs

### 🐛 Fixes

- Resolved memory leak in long-running synthesis (#234)
- Fixed race condition in parallel stack deployment (#245)

### 📈 Performance

- 50% faster synthesis for large stacks
- Reduced memory usage by 30%

### 📦 Dependencies

- AWS CDK 2.199.0 (latest)
- All security vulnerabilities patched

### 📖 Documentation

- New observability guide: [docs/observability.md](docs/observability.md)
- Updated all examples to show monitoring setup

### 💔 Breaking Changes

None in this release - fully backwards compatible!

### 🙏 Contributors

Thanks to @user1, @user2, and @user3 for their contributions!

```

### Example 2: Initial Release
```markdown
## 🎉 Initial Release v1.0.0

We're excited to announce the first stable release of CDK Common Library!

### What is CDK Common?
A comprehensive library of reusable AWS CDK constructs that simplifies infrastructure as code.

### 🎯 Key Features
- **20+ Production-Ready Constructs**: VPC, EKS, RDS, API Gateway, and more
- **Best Practices Built-in**: Security, monitoring, and cost optimization
- **Type-Safe**: Full TypeScript/Java type safety
- **Well-Tested**: 85% code coverage, production-proven

### 🚀 Getting Started
See our [Quick Start Guide](USAGE.md) to begin using CDK Common in your projects.

### 📚 Documentation
- [Usage Guide](USAGE.md)
- [API Reference](https://javadoc.io/doc/io.tinstafl/cdk-common)
- [Examples](examples/)

### 🔮 What's Next
- ECS Fargate constructs (v1.1)
- Step Functions integration (v1.2)
- Cost optimization features (v1.3)

### 🤝 Contributing
We welcome contributions! See [CONTRIBUTING.md](CONTRIBUTING.md) to get started.

### 📄 License
This project is licensed under the Apache 2.0 License.
```

## 🎓 Best Practices Summary

1. **Always test locally first**
2. **Use draft mode for review**
3. **Write user-focused release notes**
4. **Include migration guides for breaking changes**
5. **Credit contributors**
6. **Provide examples and documentation**
7. **Follow semantic versioning strictly**
8. **Keep a consistent release schedule**
9. **Automate as much as possible**
10. **Celebrate releases with your team!**

---

Remember: A professional release is not just about the code—it's about communication, documentation, and making it easy
for users to adopt your changes.