package io.stxkxs.model.aws.codebuild;

/**
 * Pipeline hosted configuration.
 *
 * @param <T>
 *          type parameter for host
 * @param <U>
 *          type parameter for hosted
 */
public record PipelineHosted<T, U>(PipelineHost<T> host, U hosted) {}
