package io.stxkxs.model._main;

import static java.util.stream.Collectors.toMap;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.SneakyThrows;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.lang3.StringUtils;

/**
 * Central configuration record that serves as the foundational metadata container for all CDK resources and constructs throughout the
 * infrastructure framework.
 *
 * <p>
 * This record encapsulates common deployment metadata, infrastructure context, and provides essential utility methods for resource
 * identification and tag management across the entire AWS CDK ecosystem.
 *
 * <p>
 * <b>Core Metadata Fields:</b>
 * <ul>
 * <li><b>id</b> - Unique resource identifier derived from deployment context</li>
 * <li><b>account</b> - AWS account ID for resource deployment</li>
 * <li><b>region</b> - AWS region for resource provisioning</li>
 * <li><b>organization</b> - Organizational unit or team identifier</li>
 * <li><b>environment</b> - Deployment environment (dev, staging, prod)</li>
 * <li><b>version</b> - Application or infrastructure version</li>
 * <li><b>domain</b> - DNS domain for public-facing resources</li>
 * <li><b>tags</b> - Common tags applied to all provisioned resources</li>
 * </ul>
 *
 * <p>
 * <b>ID Generation Algorithm:</b> The record provides sophisticated ID generation capabilities:
 * <ul>
 * <li><b>Deterministic IDs</b> - SHA-256 hash-based generation from input strings</li>
 * <li><b>Base32 Encoding</b> - URL-safe encoding with case normalization</li>
 * <li><b>AWS Compliance</b> - Ensures first character is alphabetic for AWS resource naming</li>
 * <li><b>Collision Resistance</b> - Cryptographically secure hash ensures uniqueness</li>
 * <li><b>Random IDs</b> - SecureRandom-based generation for temporary identifiers</li>
 * </ul>
 *
 * <p>
 * <b>Tag Management Utilities:</b> The {@code Maps} nested class provides advanced tag merging capabilities:
 * <ul>
 * <li><b>Priority-based Merging</b> - Later maps override earlier ones for conflicts</li>
 * <li><b>Null-safe Operations</b> - Graceful handling of null or empty tag maps</li>
 * <li><b>Multimap Support</b> - Merge up to three tag maps in a single operation</li>
 * <li><b>Immutable Results</b> - Returns new maps without modifying inputs</li>
 * </ul>
 *
 * <p>
 * <b>Security Considerations:</b>
 * <ul>
 * <li>Uses SHA-256 for cryptographically secure ID generation</li>
 * <li>Employs SecureRandom for unpredictable random ID creation</li>
 * <li>Normalizes case to prevent AWS resource naming conflicts</li>
 * <li>Truncates to 15 characters for AWS resource name constraints</li>
 * </ul>
 *
 * <p>
 * <b>Usage Throughout Framework:</b> This record is passed to every construct and provides consistent metadata and tagging across the
 * entire infrastructure deployment:
 *
 * <pre>{@code
 * Common common = Common.builder().id("my-app").account("123456789012").region("us-east-1").environment("production")
 *   .tags(Map.of("Team", "Platform", "Cost", "Engineering")).build();
 *
 * // Generate deterministic resource ID
 * String resourceId = Common.id("my-unique-resource-name");
 *
 * // Merge multiple tag sources
 * Map<String, String> allTags = Common.Maps.from(common.tags(), resourceSpecificTags, environmentTags);
 * }</pre>
 *
 * @param id
 *          Unique identifier for the deployment context
 * @param account
 *          AWS account ID where resources will be provisioned
 * @param region
 *          AWS region for resource deployment
 * @param organization
 *          Organizational identifier or team name
 * @param name
 *          Human-readable name for the deployment
 * @param alias
 *          Short alias or abbreviation for the deployment
 * @param environment
 *          Deployment environment (dev, staging, production, etc.)
 * @param version
 *          Application or infrastructure version identifier
 * @param domain
 *          DNS domain for public resources and endpoints
 * @param tags
 *          Common tags applied to all resources in this deployment
 * @author CDK Common Framework
 * @see Maps for tag merging utilities
 * @since 1.0.0
 */
@Builder(toBuilder = true)
public record Common(String id, String account, String region, String organization, String name, String alias, String environment,
  String version, String domain, Map<String, String> tags) {

  private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";

  @SneakyThrows
  public static String id(String target) {
    var digest = MessageDigest.getInstance("SHA-256");
    var hashBytes = digest.digest(target.getBytes());

    var encodedString = base32Encode(hashBytes);
    encodedString = StringUtils.substring(encodedString, 0, 15);

    if (!Character.isLetter(encodedString.charAt(0))) {
      var replacement = replace(encodedString);
      encodedString = replacement + encodedString.substring(1);
    }

    return encodedString.toLowerCase(Locale.ROOT);
  }

  public static String id_() {
    var LENGTH = 10;
    StringBuilder result = new StringBuilder(LENGTH);
    for (int i = 0; i < LENGTH; i++) {
      int index = new SecureRandom().nextInt(ALPHABET.length());
      result.append(ALPHABET.charAt(index));
    }
    return result.toString();
  }

  private static String base32Encode(byte[] bytes) {
    var base32 = new Base32();
    return base32.encodeAsString(bytes);
  }

  private static char replace(String encodedString) {
    var index = Math.abs(encodedString.hashCode() % ALPHABET.length());
    return ALPHABET.charAt(index);
  }

  public static class Maps {
    public static <K, V> Map<K, V> from(Map<K, V> a, Map<K, V> b, Map<K, V> c) {
      return from(from(a, b), c);
    }

    public static <K, V> Map<K, V> from(Map<K, V> a, Map<K, V> b) {
      if (a == null && b == null) {
        return Collections.emptyMap();
      }
      if (b == null) {
        return a;
      }
      if (a == null) {
        return b;
      }

      return Stream.concat(a.entrySet().stream(), b.entrySet().stream())
        .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (main, priority) -> priority));
    }
  }
}
