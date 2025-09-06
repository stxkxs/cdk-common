package io.stxkxs.model.aws.codebuild;

import org.apache.commons.text.WordUtils;

/**
 * Build step types for CodeBuild pipelines.
 */
public enum BuildStep {
  BUILD, ASSETS, DEPLOY;

  /**
   * Returns the formatted string value of this build step.
   *
   * @return formatted build step value
   */
  public String value() {
    if (this.equals(BUILD)) {
      return BUILD.toString().toLowerCase();
    }
    return WordUtils.capitalizeFully(this.toString());
  }
}
