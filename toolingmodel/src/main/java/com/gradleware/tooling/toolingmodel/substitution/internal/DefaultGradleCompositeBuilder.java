package com.gradleware.tooling.toolingmodel.substitution.internal;

import com.gradleware.tooling.toolingmodel.substitution.GradleCompositeBuild;
import com.gradleware.tooling.toolingmodel.substitution.GradleCompositeBuilder;
import org.gradle.internal.concurrent.DefaultExecutorFactory;
import org.gradle.internal.concurrent.ExecutorFactory;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.internal.consumer.*;
import org.gradle.tooling.internal.consumer.loader.CachingToolingImplementationLoader;
import org.gradle.tooling.internal.consumer.loader.DefaultToolingImplementationLoader;
import org.gradle.tooling.internal.consumer.loader.SynchronizedToolingImplementationLoader;
import org.gradle.tooling.internal.consumer.loader.ToolingImplementationLoader;

import java.util.HashSet;
import java.util.Set;

public class DefaultGradleCompositeBuilder extends GradleCompositeBuilder {

    private static final ToolingImplementationLoader toolingImplementationLoader = new SynchronizedToolingImplementationLoader(new CachingToolingImplementationLoader(new DefaultToolingImplementationLoader()));
    private static final ExecutorFactory executorFactory =  new DefaultExecutorFactory();
    private static final LoggingProvider loggingProvider = new SynchronizedLogging();
    private static final ExecutorServiceFactory executorServiceFactory = new DefaultExecutorServiceFactory();
    private static final DistributionFactory distributionFactory = new DistributionFactory(executorServiceFactory);
    private static final DefaultConnectionParameters.Builder connectionParamsBuilder = DefaultConnectionParameters.builder();
    private final Set<ProjectConnection> participants = new HashSet<ProjectConnection>();

    @Override
    public GradleCompositeBuilder withParticipant(ProjectConnection participant) {
        participants.add(participant);
        return this;
    }

    @Override
    public GradleCompositeBuild build() {
        ConnectionParameters connectionParameters = connectionParamsBuilder.build();
        Distribution distribution = distributionFactory.getDefaultDistribution(connectionParameters.getProjectDir(), connectionParameters.isSearchUpwards() != null ? connectionParameters.isSearchUpwards() : true);
        GradleCompositeFactory gradleCompositeFactory = new GradleCompositeFactory(toolingImplementationLoader, executorFactory, loggingProvider, participants);
        return gradleCompositeFactory.create(distribution, connectionParameters);
    }
}
