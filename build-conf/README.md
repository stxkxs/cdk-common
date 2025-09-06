# Build Configuration

This directory contains modularized Maven build configuration files for better organization and maintainability.

## Structure

```
build-conf/
├── versions.properties          # All version properties in one place
├── dependencies-aws.xml         # AWS CDK and SDK dependencies
├── dependencies-core.xml        # Core library dependencies (Jackson, Commons, etc.)
├── dependencies-test.xml        # Testing dependencies (JUnit)
├── plugins-build.xml           # Core build plugins (compiler)
├── plugins-testing.xml         # Testing plugins (Surefire, JaCoCo)
├── plugins-quality.xml         # Code quality plugins (PMD, Checkstyle, SpotBugs, Spotless)
└── plugins-security.xml        # Security plugins (OWASP Dependency Check)
```

## Purpose

These files serve as:
1. **Documentation** - Clear organization of what dependencies and plugins are used for what purpose
2. **Reference** - Easy to copy/paste when creating new projects
3. **Maintenance** - Single location to review and update versions

## Usage Options

### Option 1: Properties File Import (Partial Support)
Maven can load external properties files using the Properties Maven Plugin:

```xml
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>properties-maven-plugin</artifactId>
    <version>1.2.1</version>
    <executions>
        <execution>
            <phase>initialize</phase>
            <goals>
                <goal>read-project-properties</goal>
            </goals>
            <configuration>
                <files>
                    <file>build-conf/versions.properties</file>
                </files>
            </configuration>
        </execution>
    </executions>
</plugin>
```

### Option 2: Maven Tiles (Advanced)
Use Maven Tiles plugin to include external configuration fragments:

```xml
<plugin>
    <groupId>io.repaint.maven</groupId>
    <artifactId>tiles-maven-plugin</artifactId>
    <version>2.40</version>
    <extensions>true</extensions>
</plugin>
```

### Option 3: Parent POM
Create a parent POM that contains all the dependency management and plugin management, then inherit from it.

### Option 4: Manual Reference
Keep these files as documentation and manually maintain the main pom.xml with reference to these organized configurations.

## Version Management

All versions are centralized in `versions.properties` for easy updates:
- AWS versions are grouped together
- Plugin versions are clearly identified
- Test framework versions are separate

## Updating Dependencies

1. Check for updates: `mvn versions:display-dependency-updates`
2. Update version in `versions.properties`
3. Sync with main `pom.xml` if not using automatic import
4. Run tests to ensure compatibility

## Adding New Dependencies

1. Determine the category (AWS, Core, Test, etc.)
2. Add version to `versions.properties`
3. Add dependency to appropriate `dependencies-*.xml` file
4. Update main `pom.xml`

## Security Scanning

Run OWASP dependency check:
```bash
mvn dependency-check:check
```

## Code Quality Checks

Run all quality checks:
```bash
mvn clean verify pmd:check checkstyle:check spotbugs:check
```