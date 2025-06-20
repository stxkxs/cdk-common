package io.stxkxs.execute.serialization;

import io.stxkxs.model._main.Common;
import org.apache.commons.lang3.StringUtils;
import software.constructs.Construct;

import java.util.Optional;

public class Format {
  public static String id(String... s) {
    return String.join(".", s)
      .replace("-", ".");
  }

  public static String name(String... s) {
    return id(s).replace(".", "-");
  }

  public static String describe(Common common, String... s) {
    return String.format("%s %s %s",
      common.organization(), common.environment(),
      StringUtils.join(s, " "));
  }

  public static String exported(Construct scope, String suffix) {
    var prefix = Optional.of(scope)
      .map(s -> s.getNode().tryGetContext("hosted:synthesizer:name"))
      .map(Object::toString)
      .orElseGet(() -> scope.getNode().getContext("host:id").toString());;

    var hostedId = scope.getNode().getContext("hosted:id");
    return String.format("%s%s%s", prefix, hostedId, suffix);
  }

  public static String named(Construct scope, String suffix) {
    var prefix = Optional.of(scope)
      .map(s -> s.getNode().tryGetContext("hosted:synthesizer:name"))
      .map(Object::toString)
      .orElseGet(() -> scope.getNode().getContext("host:id").toString());;

    var hostedId = scope.getNode().getContext("hosted:id");
    return String.format("%s-%s-%s", prefix, hostedId, suffix);
  }
}
