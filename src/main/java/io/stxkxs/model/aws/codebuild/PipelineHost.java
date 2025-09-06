package io.stxkxs.model.aws.codebuild;

import io.stxkxs.model._main.Common;

/**
 * Pipeline host configuration.
 *
 * @param <T>
 *          type parameter for generic host
 */
public record PipelineHost<T>(Common common, CodeStarConnectionSource source, Pipeline pipeline, String synthesizer) {}
