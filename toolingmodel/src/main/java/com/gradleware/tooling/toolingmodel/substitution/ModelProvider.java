package com.gradleware.tooling.toolingmodel.substitution;

import org.gradle.tooling.ModelBuilder;

public interface ModelProvider {

    <T> T getModel(Class<T> modelType);

    /**
     * Creates a builder which can be used to query the model of the given type.
     *
     * <p>Any of following models types may be available, depending on the version of Gradle being used by the target
     * build:
     *
     * <ul>
     *     <li>{@link com.gradleware.tooling.toolingmodel.substitution.EclipseWorkspace}</li>
     * </ul>
     * @param modelType The model type
     * @param <T> The model type.
     * @return The builder.
     */
    <T> ModelBuilder<T> model(Class<T> modelType);
}
