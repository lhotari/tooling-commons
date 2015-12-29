package com.gradleware.tooling.toolingmodel.substitution;

import com.gradleware.tooling.toolingmodel.substitution.internal.GradleWorkspaceFactory;
import org.gradle.internal.concurrent.DefaultExecutorFactory;
import org.gradle.internal.concurrent.ExecutorFactory;
import org.gradle.tooling.internal.consumer.*;
import org.gradle.tooling.internal.consumer.loader.CachingToolingImplementationLoader;
import org.gradle.tooling.internal.consumer.loader.DefaultToolingImplementationLoader;
import org.gradle.tooling.internal.consumer.loader.SynchronizedToolingImplementationLoader;
import org.gradle.tooling.internal.consumer.loader.ToolingImplementationLoader;

// this would be added to the original GradleConnector
public abstract class GradleConnector {
    private static final ToolingImplementationLoader toolingImplementationLoader = new SynchronizedToolingImplementationLoader(new CachingToolingImplementationLoader(new DefaultToolingImplementationLoader()));
    private static final ExecutorFactory executorFactory =  new DefaultExecutorFactory();
    private static final LoggingProvider loggingProvider = new SynchronizedLogging();
    private static final ExecutorServiceFactory executorServiceFactory = new DefaultExecutorServiceFactory();
    private static final DistributionFactory distributionFactory = new DistributionFactory(executorServiceFactory);
    private static final DefaultConnectionParameters.Builder connectionParamsBuilder = DefaultConnectionParameters.builder();

    public static GradleWorkspace newWorkspace() {
        ConnectionParameters connectionParameters = connectionParamsBuilder.build();
        Distribution distribution = distributionFactory.getDefaultDistribution(connectionParameters.getProjectDir(), connectionParameters.isSearchUpwards() != null ? connectionParameters.isSearchUpwards() : true);

        GradleWorkspaceFactory gradleWorkspaceFactory = new GradleWorkspaceFactory(toolingImplementationLoader, executorFactory, loggingProvider);
        return gradleWorkspaceFactory.create(distribution, connectionParameters);
    }
}
