package com.gradleware.tooling.toolingmodel.substitution;

import org.gradle.tooling.GradleConnectionException;

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
}
