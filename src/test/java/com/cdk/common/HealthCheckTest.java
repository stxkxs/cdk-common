package com.cdk.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Simple health check test to ensure test framework is working and generates test reports for CI/CD pipeline.
 */
public class HealthCheckTest {

  @Test
  public void testHealthCheck() {
    // simple assertion to verify test framework is operational
    assertTrue(true, "health check should always pass");
  }

  @Test
  public void testBasicMath() {
    // basic math test to ensure assertions work correctly
    var result = 2 + 2;
    assertEquals(4, result, "basic addition should work");
  }

  @Test
  public void testStringOperations() {
    // test string operations
    var message = "cdk common";
    assertNotNull(message, "message should not be null");
    assertTrue(message.contains("cdk"), "message should contain 'cdk'");
    assertEquals(10, message.length(), "message length should be 10");
  }

  @Test
  public void testTextBlock() {
    // use java 15+ text blocks
    var json = """
      {
          "project": "cdk-common",
          "type": "infrastructure",
          "version": "1.0.0"
      }
      """;
    assertNotNull(json, "json text block should not be null");
    assertTrue(json.contains("cdk-common"), "json should contain project name");
  }

  @Test
  public void testRecordPattern() {
    // use java 21 record patterns
    record Config(String name, int value) {}

    var config = new Config("test", 42);

    // pattern matching with records
    var result = switch (config) {
      case Config(var name, var value) when value > 40 -> "high value config: " + name;
      case Config(var name, var value) -> "normal config: " + name;
    };

    assertEquals("high value config: test", result, "record pattern matching should work");
  }
}
