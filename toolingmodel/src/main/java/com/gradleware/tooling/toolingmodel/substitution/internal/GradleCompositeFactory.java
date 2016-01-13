package com.gradleware.tooling.toolingmodel.substitution.internal;

import com.gradleware.tooling.toolingmodel.substitution.GradleCompositeBuild;
import org.gradle.internal.concurrent.ExecutorFactory;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.internal.consumer.ConnectionParameters;
import org.gradle.tooling.internal.consumer.Distribution;
import org.gradle.tooling.internal.consumer.LoggingProvider;
import org.gradle.tooling.internal.consumer.async.AsyncConsumerActionExecutor;
import org.gradle.tooling.internal.consumer.async.DefaultAsyncConsumerActionExecutor;
import org.gradle.tooling.internal.consumer.connection.ConsumerActionExecutor;
import org.gradle.tooling.internal.consumer.connection.LazyConsumerActionExecutor;
import org.gradle.tooling.internal.consumer.connection.ProgressLoggingConsumerActionExecutor;
import org.gradle.tooling.internal.consumer.connection.RethrowingErrorsConsumerActionExecutor;
import org.gradle.tooling.internal.consumer.loader.ToolingImplementationLoader;

import java.util.Set;

public class GradleCompositeFactory {

    private final ToolingImplementationLoader toolingImplementationLoader;
    private final ExecutorFactory executorFactory;
    private final LoggingProvider loggingProvider;
    private final Set<ProjectConnection> participants;

    public GradleCompositeFactory(ToolingImplementationLoader toolingImplementationLoader, ExecutorFactory executorFactory,
                                  LoggingProvider loggingProvider, Set<ProjectConnection> participants) {
        this.toolingImplementationLoader = toolingImplementationLoader;
        this.executorFactory = executorFactory;
        this.loggingProvider = loggingProvider;
        this.participants = participants;
    }

    public GradleCompositeBuild create(Distribution distribution, ConnectionParameters parameters) {
        ConsumerActionExecutor lazyConnection = new LazyConsumerActionExecutor(distribution, toolingImplementationLoader, loggingProvider, parameters);
        ConsumerActionExecutor progressLoggingConnection = new ProgressLoggingConsumerActionExecutor(lazyConnection, loggingProvider);
        ConsumerActionExecutor rethrowingErrorsConnection = new RethrowingErrorsConsumerActionExecutor(progressLoggingConnection);
        AsyncConsumerActionExecutor asyncConnection = new DefaultAsyncConsumerActionExecutor(rethrowingErrorsConnection, executorFactory);
        return new DefaultGradleCompositeBuild(asyncConnection, parameters, participants);
    }
}
