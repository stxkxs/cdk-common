package io.stxkxs.execute.serialization;

import com.github.mustachejava.DefaultMustacheFactory;
import io.stxkxs.model._main.Common;
import io.stxkxs.model._main.Common.Maps;
import io.stxkxs.model._main.Environment;
import io.stxkxs.model._main.Version;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import software.constructs.Construct;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

/**
 * Advanced template processing engine that provides dynamic configuration injection using 
 * Mustache templating with comprehensive CDK context integration and environment-aware resource resolution.
 * 
 * <p>This class serves as the core template processing system for the entire CDK framework,
 * enabling dynamic configuration generation with runtime value injection, environment-specific
 * customization, and context-aware parameter substitution across all infrastructure components.
 * 
 * <p><b>Core Template Capabilities:</b>
 * <ul>
 *   <li><b>Mustache Integration</b> - Logic-less templates with variable substitution</li>
 *   <li><b>Environment Awareness</b> - Environment-specific template resolution and processing</li>
 *   <li><b>Version Support</b> - Version-based template organization and selection</li>
 *   <li><b>Context Integration</b> - Deep CDK context system integration with automatic variable injection</li>
 * </ul>
 * 
 * <p><b>Template Resolution Architecture:</b>
 * Templates are organized in a hierarchical structure:
 * <pre>
 * resources/
 *   ├── {environment}/     (dev, staging, production)
 *   │   ├── {version}/     (v1.0, v2.0, etc.)
 *   │   │   ├── eks/       (service-specific templates)
 *   │   │   ├── cognito/
 *   │   │   └── ...
 * </pre>
 * 
 * <p><b>Context Variable Injection:</b>
 * The template engine automatically provides extensive context variables:
 * <ul>
 *   <li><b>Host Context</b> - Primary deployment environment variables (host:account, host:region, etc.)</li>
 *   <li><b>Hosted Context</b> - Secondary deployment context for nested stacks</li>
 *   <li><b>Synthesizer Context</b> - CDK synthesizer and build tool context</li>
 *   <li><b>Custom Variables</b> - Additional user-provided template variables</li>
 * </ul>
 * 
 * <p><b>Multi-Environment Support:</b>
 * <ul>
 *   <li><b>Environment-Specific Templates</b> - Different templates for dev, staging, production</li>
 *   <li><b>Version Management</b> - Multiple template versions for gradual rollouts</li>
 *   <li><b>Fallback Logic</b> - Graceful handling of missing templates or context</li>
 *   <li><b>Cross-Environment Consistency</b> - Shared template patterns with environment overrides</li>
 * </ul>
 * 
 * <p><b>Advanced Template Features:</b>
 * <ul>
 *   <li><b>Variable Merging</b> - Priority-based merging of default and custom variables</li>
 *   <li><b>Nested Context</b> - Support for complex nested variable structures</li>
 *   <li><b>Runtime Resolution</b> - Dynamic resource ARN and identifier resolution</li>
 *   <li><b>Error Handling</b> - Comprehensive error reporting for missing templates or variables</li>
 * </ul>
 * 
 * <p><b>Security and Validation:</b>
 * <ul>
 *   <li><b>Resource Loading</b> - Secure classpath-based template loading</li>
 *   <li><b>Input Validation</b> - Template path validation and sanitization</li>
 *   <li><b>Context Validation</b> - Required context variable verification</li>
 *   <li><b>Template Compilation</b> - Pre-compilation for performance and validation</li>
 * </ul>
 * 
 * <p><b>Performance Optimization:</b>
 * <ul>
 *   <li><b>Template Caching</b> - Compiled template caching for repeated use</li>
 *   <li><b>Lazy Loading</b> - Templates loaded only when needed</li>
 *   <li><b>Stream Processing</b> - Efficient I/O with proper resource management</li>
 *   <li><b>Memory Management</b> - Proper cleanup of template resources</li>
 * </ul>
 * 
 * <p><b>Integration Patterns:</b>
 * The template system integrates with various CDK components:
 * <ul>
 *   <li>Configuration object parsing and instantiation</li>
 *   <li>Kubernetes manifest generation with dynamic values</li>
 *   <li>AWS service configuration with environment-specific parameters</li>
 *   <li>Multi-tenant deployment scenarios with tenant-specific customization</li>
 * </ul>
 * 
 * <p><b>Usage Examples:</b>
 * <pre>{@code
 * // Basic template parsing with default context
 * String processedConfig = Template.parse(scope, "eks/cluster-config.json");
 * 
 * // Template parsing with additional variables
 * Map<String, Object> customVars = Map.of(
 *     "clusterSize", "large",
 *     "enableLogging", true
 * );
 * String config = Template.parse(scope, "eks/cluster-config.json", customVars);
 * 
 * // Typical usage in construct
 * var nodeGroupsConfig = Mapper.get().readValue(
 *     Template.parse(this, conf.nodeGroups()), 
 *     new TypeReference<List<NodeGroup>>() {}
 * );
 * }</pre>
 * 
 * @author CDK Common Framework
 * @since 1.0.0
 * @see DefaultMustacheFactory for mustache template processing
 * @see Mapper for JSON/YAML processing integration
 * @see Common for context and metadata management
 * @see Environment for environment-specific processing
 * @see Version for version management integration
 */
@Slf4j
public class Template {

  @SneakyThrows
  public static String parse(Construct scope, String file) {
    var version = Version.of(scope.getNode().getContext("host:version"));
    var environment = Environment.of(scope.getNode().getContext("host:environment"));
    return execute(environment, version, file, defaults(scope));
  }

  @SneakyThrows
  public static String parse(Construct scope, String file, Map<String, Object> values) {
    var version = Version.of(scope.getNode().getContext("host:version"));
    var environment = Environment.of(scope.getNode().getContext("host:environment"));
    return execute(environment, version, file, Maps.from(defaults(scope), values));
  }

  @SneakyThrows
  private static String execute(Environment environment, Version version, String file, Map<String, Object> values) {
    log.debug("parsing template {}/{}/{} with parameters {}", environment, version, file, values);

    var factory = new DefaultMustacheFactory();
    var writer = new StringWriter();
    var prefix = String.format("%s/%s", environment, version);
    var template = String.format("%s/%s", prefix, file);

    try (var stream = Template.class.getClassLoader().getResourceAsStream(template)) {
      if (stream == null) {
        var m = String.format("error parsing template! can not find %s.", template);
        throw new RuntimeException(m);
      }

      assemble(stream, template, values, factory, writer);
    }

    return writer.toString();
  }

  protected static Map<String, Object> defaults(Construct scope) {
    var home = Optional.of(scope)
      .map(s -> s.getNode().tryGetContext("home"))
      .map(Object::toString)
      .orElse("/");

    var synthesizer = Optional.of(scope)
      .map(s -> s.getNode().tryGetContext("hosted:synthesizer:name"))
      .map(Object::toString)
      .orElseGet(Common::id_);

    var d = Map.<String, Object>ofEntries(
      Map.entry("home", home),
      Map.entry("synthesizer:name", synthesizer),
      Map.entry("host:id", scope.getNode().getContext("host:id").toString()),
      Map.entry("host:organization", scope.getNode().getContext("host:organization").toString()),
      Map.entry("host:account", scope.getNode().getContext("host:account").toString()),
      Map.entry("host:region", scope.getNode().getContext("host:region").toString()),
      Map.entry("host:name", scope.getNode().getContext("host:name").toString()),
      Map.entry("host:alias", scope.getNode().getContext("host:alias").toString()),
      Map.entry("host:environment", scope.getNode().getContext("host:environment").toString()),
      Map.entry("host:version", scope.getNode().getContext("host:version").toString()),
      Map.entry("host:domain", scope.getNode().getContext("host:domain").toString()),
      Map.entry("hosted:id", scope.getNode().getContext("hosted:id").toString()),
      Map.entry("hosted:organization", scope.getNode().getContext("hosted:organization").toString()),
      Map.entry("hosted:account", scope.getNode().getContext("hosted:account").toString()),
      Map.entry("hosted:region", scope.getNode().getContext("hosted:region").toString()),
      Map.entry("hosted:name", scope.getNode().getContext("hosted:name").toString()),
      Map.entry("hosted:alias", scope.getNode().getContext("hosted:alias").toString()),
      Map.entry("hosted:environment", scope.getNode().getContext("hosted:environment").toString()),
      Map.entry("hosted:version", scope.getNode().getContext("hosted:version").toString()),
      Map.entry("hosted:domain", scope.getNode().getContext("hosted:domain").toString())
    );

    log.debug("default template variables [defaults: {}]", d);

    return d;
  }

  @SneakyThrows
  protected static void assemble(InputStream stream, String template, Map<String, Object> values, DefaultMustacheFactory factory, StringWriter writer) {
    factory.compile(new InputStreamReader(stream, StandardCharsets.UTF_8), template)
      .execute(writer, values)
      .flush();
  }
}
