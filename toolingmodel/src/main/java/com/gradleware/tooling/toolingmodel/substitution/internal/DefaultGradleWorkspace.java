package com.gradleware.tooling.toolingmodel.substitution.internal;

import com.gradleware.tooling.toolingmodel.substitution.GradleWorkspace;
import com.gradleware.tooling.toolingmodel.substitution.ProjectIdentity;
import org.gradle.tooling.ModelBuilder;
import org.gradle.tooling.internal.consumer.ConnectionParameters;
import org.gradle.tooling.internal.consumer.DefaultModelBuilder;
import org.gradle.tooling.internal.consumer.async.AsyncConsumerActionExecutor;

import java.util.HashSet;
import java.util.Set;

public class DefaultGradleWorkspace implements GradleWorkspace {

    private final AsyncConsumerActionExecutor connection;
    private final ConnectionParameters parameters;
    private final Set<ProjectIdentity> projectIdentities = new HashSet<ProjectIdentity>();

    public DefaultGradleWorkspace(AsyncConsumerActionExecutor connection, ConnectionParameters parameters) {
        this.connection = connection;
        this.parameters = parameters;
    }

    @Override
    public void addProject(ProjectIdentity projectIdentity) {
        projectIdentities.add(projectIdentity);
    }

    @Override
    public void removeProject(ProjectIdentity projectIdentity) {
        projectIdentities.remove(projectIdentity);
    }

    @Override
    public <T> T getModel(Class<T> modelType) {
        return model(modelType).get();
    }

    @Override
    public <T> ModelBuilder<T> model(Class<T> modelType) {
        if (!modelType.isInterface()) {
            throw new IllegalArgumentException(String.format("Cannot fetch a model of type '%s' as this type is not an interface.", modelType.getName()));
        }

        // TODO: need to pass on project identities for model creation
        // probably requires a different implementation of ModelBuilder
        // I don't think we'll want to pass them on in ConnectionParameters
        return new DefaultModelBuilder<T>(modelType, connection, parameters);
    }
}