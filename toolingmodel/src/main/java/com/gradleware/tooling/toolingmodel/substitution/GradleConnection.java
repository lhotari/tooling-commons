package com.gradleware.tooling.toolingmodel.substitution;

import org.gradle.tooling.GradleConnectionException;

public interface GradleConnection {

    <T> T getModel(Class<T> modelType) throws GradleConnectionException, IllegalStateException;
}
