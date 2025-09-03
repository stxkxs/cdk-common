package io.stxkxs.execute.serialization;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSetter.Value;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.DefaultAccessorNamingStrategy.Provider;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import static com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.WRITE_DOC_START_MARKER;

/**
 * Sophisticated JSON/YAML object mapping utility that provides a pre-configured Jackson ObjectMapper
 * with advanced serialization features optimized for CDK infrastructure configuration processing.
 * 
 * <p>This singleton mapper serves as the central serialization engine for the entire CDK framework,
 * handling complex configuration objects, template processing, and data transformation between
 * Java objects and JSON/YAML representations.
 * 
 * <p><b>Core Configuration Features:</b>
 * <ul>
 *   <li><b>YAML Support</b> - Native YAML parsing and generation without document markers</li>
 *   <li><b>Case Insensitive Processing</b> - Flexible enum and property name matching</li>
 *   <li><b>Null Handling</b> - Non-null serialization with intelligent null-to-empty conversions</li>
 *   <li><b>Java 8+ Support</b> - Optional, Stream, and modern Java features integration</li>
 * </ul>
 * 
 * <p><b>Advanced Serialization Behavior:</b>
 * <ul>
 *   <li><b>Collection Handling</b> - Null collections automatically converted to empty collections</li>
 *   <li><b>Map Processing</b> - LinkedHashMap and TreeSet null-safety with order preservation</li>
 *   <li><b>Custom Accessor Naming</b> - Simplified property naming without traditional get/set prefixes</li>
 *   <li><b>Enum Flexibility</b> - Case-insensitive enum deserialization for configuration robustness</li>
 * </ul>
 * 
 * <p><b>CDK-Optimized Configuration:</b>
 * The mapper is specifically tuned for CDK infrastructure configuration patterns:
 * <ul>
 *   <li><b>Template Processing</b> - Seamless integration with mustache template system</li>
 *   <li><b>Configuration Objects</b> - Optimized for complex nested configuration structures</li>
 *   <li><b>AWS Service Models</b> - Handles AWS service configuration with flexible property mapping</li>
 *   <li><b>Record Support</b> - Full Java record serialization and deserialization</li>
 * </ul>
 * 
 * <p><b>Null Safety Architecture:</b>
 * The mapper implements comprehensive null safety strategies:
 * <ul>
 *   <li><b>Non-null Serialization</b> - Excludes null values from output JSON/YAML</li>
 *   <li><b>Empty Collection Defaults</b> - Null collections become empty collections during deserialization</li>
 *   <li><b>Map Safety</b> - Null maps converted to empty maps with proper generic type preservation</li>
 *   <li><b>List Safety</b> - Null lists converted to empty lists maintaining type information</li>
 * </ul>
 * 
 * <p><b>YAML Processing Specialization:</b>
 * The mapper uses a customized YAML factory with:
 * <ul>
 *   <li>Disabled document start markers for cleaner output</li>
 *   <li>Optimized for Kubernetes manifest processing</li>
 *   <li>Enhanced compatibility with template systems</li>
 *   <li>Consistent formatting across different deployment environments</li>
 * </ul>
 * 
 * <p><b>Performance Considerations:</b>
 * <ul>
 *   <li><b>Singleton Pattern</b> - Single mapper instance for optimal performance</li>
 *   <li><b>Pre-configured</b> - No runtime configuration overhead</li>
 *   <li><b>Thread Safe</b> - Safe for concurrent use across multiple constructs</li>
 *   <li><b>Optimized Parsing</b> - Tuned for large configuration object processing</li>
 * </ul>
 * 
 * <p><b>Usage Throughout Framework:</b>
 * This mapper is used extensively for configuration processing:
 * <pre>{@code
 * // Parse configuration from JSON/YAML
 * ObjectMapper mapper = Mapper.get();
 * MyConfigClass config = mapper.readValue(jsonString, MyConfigClass.class);
 * 
 * // Serialize configuration to JSON
 * String json = mapper.writeValueAsString(configObject);
 * 
 * // Handle complex nested structures
 * ComplexConfig config = mapper.readValue(
 *     templateOutput, 
 *     new TypeReference<ComplexConfig>() {}
 * );
 * 
 * // Process collections with type safety
 * List<NodeGroup> nodeGroups = mapper.readValue(
 *     nodeGroupsJson, 
 *     new TypeReference<List<NodeGroup>>() {}
 * );
 * }</pre>
 * 
 * @author CDK Common Framework
 * @since 1.0.0
 * @see ObjectMapper for Jackson ObjectMapper documentation
 * @see Template for template processing integration
 * @see DefaultMixin for serialization behavior customization
 */
public class Mapper {
  private static final ObjectMapper mapper = configure();

  private Mapper() {}

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

  private static YAMLFactory yamlConf() {
    return YAMLFactory.builder()
      .disable(WRITE_DOC_START_MARKER)
      .build();
  }

  public static ObjectMapper get() {return mapper;}
}
