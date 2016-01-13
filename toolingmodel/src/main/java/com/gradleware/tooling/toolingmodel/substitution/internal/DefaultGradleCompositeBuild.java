package com.gradleware.tooling.toolingmodel.substitution.internal;

import com.gradleware.tooling.toolingmodel.substitution.EclipseWorkspace;
import com.gradleware.tooling.toolingmodel.substitution.GradleCompositeBuild;
import org.gradle.tooling.GradleConnectionException;
import org.gradle.tooling.ModelBuilder;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.ResultHandler;
import org.gradle.tooling.internal.consumer.ConnectionParameters;
import org.gradle.tooling.internal.consumer.async.AsyncConsumerActionExecutor;

import java.util.Set;

public class DefaultGradleCompositeBuild implements GradleCompositeBuild {

    private final AsyncConsumerActionExecutor connection;
    private final ConnectionParameters parameters;
    private final Set<ProjectConnection> participants;

    public DefaultGradleCompositeBuild(AsyncConsumerActionExecutor connection, ConnectionParameters parameters, Set<ProjectConnection> participants) {
        this.connection = connection;
        this.parameters = parameters;
        this.participants = participants;
    }

    /**
     * {@inheritDoc}
     *
     * The only allowed model type that can be requested is {@link com.gradleware.tooling.toolingmodel.substitution.EclipseWorkspace}.
     */
    @Override
    public <T> T getModel(Class<T> modelType) throws GradleConnectionException, IllegalStateException {
        return model(modelType).get();
    }

    /**
     * {@inheritDoc}
     *
     * The only allowed model type that can be requested is {@link com.gradleware.tooling.toolingmodel.substitution.EclipseWorkspace}.
     */
    @Override
    public <T> void getModel(Class<T> modelType, ResultHandler<? super T> handler) throws IllegalStateException {
        model(modelType).get(handler);
    }

    /**
     * {@inheritDoc}
     *
     * The only allowed model type that can be requested is {@link com.gradleware.tooling.toolingmodel.substitution.EclipseWorkspace}.
     */
    @Override
    public <T> ModelBuilder<T> model(Class<T> modelType) {
        if (!modelType.isInterface()) {
            throw new IllegalArgumentException(String.format("Cannot fetch a model of type '%s' as this type is not an interface.", modelType.getName()));
        }

        if (!modelType.equals(EclipseWorkspace.class)) {
            throw new IllegalArgumentException("The only supported model for a Gradle composite is EclipseWorkspace.class.");
        }

        return new EclipseWorkspaceModelBuilder<T>(modelType, connection, parameters, participants);
    }
}
