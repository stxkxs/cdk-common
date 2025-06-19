package io.stxkxs.model.aws.codebuild;

import org.apache.commons.text.WordUtils;

public enum BuildStep {
  BUILD, ASSETS, DEPLOY;

  public String value() {
    if (this.equals(BUILD))
      return BUILD.toString().toLowerCase();
    return WordUtils.capitalizeFully(this.toString());
  }
}
