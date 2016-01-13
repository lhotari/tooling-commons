package com.gradleware.tooling.toolingmodel.substitution;

import org.gradle.tooling.GradleConnectionException;
import org.gradle.tooling.ModelBuilder;
import org.gradle.tooling.ResultHandler;

public interface GradleConnection {

    /**
     * Retrieves the model for a Gradle connection.
     *
     * @param modelType model type
     * @param <T> requested model
     * @return Model
     * @throws GradleConnectionException
     * @throws IllegalStateException
     */
    <T> T getModel(Class<T> modelType) throws GradleConnectionException, IllegalStateException;

    /**
     * Starts fetching a snapshot of the given model, passing the result to the given handler when complete. This method returns immediately, and the result is later
     * passed to the given handler's {@link ResultHandler#onComplete(Object)} method.
     *
     * @param modelType The model type.
     * @param handler The handler to pass the result to.
     * @param <T> The model type.
     * @throws IllegalStateException When this connection has been closed or is closing.
     */
    <T> void getModel(Class<T> modelType, ResultHandler<? super T> handler) throws IllegalStateException;

    /**
     * Creates a builder which can be used to query the model of the given type.
     *
     * @param modelType The model type
     * @param <T> The model type.
     * @return The builder.
     */
    <T> ModelBuilder<T> model(Class<T> modelType);
}
