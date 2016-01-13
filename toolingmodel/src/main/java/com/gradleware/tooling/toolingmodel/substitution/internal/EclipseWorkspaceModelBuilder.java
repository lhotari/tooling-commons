package com.gradleware.tooling.toolingmodel.substitution.internal;

import com.gradleware.tooling.toolingmodel.substitution.deduper.EclipseProjectDeduper;
import org.gradle.internal.UncheckedException;
import org.gradle.internal.event.ListenerNotificationException;
import org.gradle.tooling.*;
import org.gradle.tooling.exceptions.UnsupportedBuildArgumentException;
import org.gradle.tooling.exceptions.UnsupportedOperationConfigurationException;
import org.gradle.tooling.internal.consumer.AbstractLongRunningOperation;
import org.gradle.tooling.internal.consumer.ConnectionParameters;
import org.gradle.tooling.internal.consumer.async.AsyncConsumerActionExecutor;
import org.gradle.tooling.internal.consumer.connection.ConsumerAction;
import org.gradle.tooling.internal.consumer.connection.ConsumerConnection;
import org.gradle.tooling.internal.consumer.parameters.ConsumerOperationParameters;
import org.gradle.tooling.internal.protocol.BuildExceptionVersion1;
import org.gradle.tooling.internal.protocol.InternalBuildCancelledException;
import org.gradle.tooling.internal.protocol.ResultHandlerVersion1;
import org.gradle.tooling.internal.protocol.exceptions.InternalUnsupportedBuildArgumentException;
import org.gradle.tooling.internal.protocol.test.InternalTestExecutionException;
import org.gradle.tooling.model.DomainObjectSet;
import org.gradle.tooling.model.UnsupportedMethodException;
import org.gradle.tooling.model.eclipse.EclipseProject;
import org.gradle.tooling.model.internal.Exceptions;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class EclipseWorkspaceModelBuilder<T> extends AbstractLongRunningOperation<EclipseWorkspaceModelBuilder<T>> implements ModelBuilder<T> {
    private final Class<T> modelType;
    private final AsyncConsumerActionExecutor connection;
    private final Set<ProjectConnection> participants;

    public EclipseWorkspaceModelBuilder(Class<T> modelType, AsyncConsumerActionExecutor connection, ConnectionParameters parameters, Set<ProjectConnection> participants) {
        super(parameters);
        this.modelType = modelType;
        this.connection = connection;
        this.participants = participants;
        operationParamsBuilder.setEntryPoint("ModelBuilder API");
    }

    @Override
    protected EclipseWorkspaceModelBuilder<T> getThis() {
        return this;
    }

    @Override
    public ModelBuilder<T> forTasks(String... tasks) {
        // only set a non-null task list on the operationParamsBuilder if at least one task has been given to this method,
        // this is needed since any non-null list, even if empty, is treated as 'execute these tasks before building the model'
        // this would cause an error when fetching the BuildEnvironment model
        List<String> rationalizedTasks = rationalizeInput(tasks);
        operationParamsBuilder.setTasks(rationalizedTasks);
        return this;
    }

    @Override
    public ModelBuilder<T> forTasks(Iterable<String> tasks) {
        operationParamsBuilder.setTasks(rationalizeInput(tasks));
        return this;
    }

    @Override
    public T get() throws GradleConnectionException, IllegalStateException {
        BlockingResultHandler<T> handler = new BlockingResultHandler<T>(modelType);
        get(handler);
        return handler.getResult();
    }

    @Override
    public void get(ResultHandler<? super T> handler) throws IllegalStateException {
        final ConsumerOperationParameters operationParameters = getConsumerOperationParameters();
        connection.run(new ConsumerAction<T>() {
            public ConsumerOperationParameters getParameters() {
                return operationParameters;
            }
            public T run(ConsumerConnection connection) {
                Set<EclipseProject> openProjects = deduplicate(populateModel());
                return (T) new DefaultEclipseWorkspace(openProjects);
            }
        }, new DefaultResultHandler<T>(handler));
    }

    private Set<EclipseProject> deduplicate(Set<EclipseProject> openProjects) {
        Set<EclipseProject> projects = new HashSet<EclipseProject>();
        projects.addAll(new EclipseProjectDeduper().dedup(openProjects));
        return projects;
    }

    /**
     * Returns the set of projects found on any level of the hierarchy. Excludes the root project.
     *
     * @return set of projects
     */
    private Set<EclipseProject> populateModel() {
        Set<EclipseProject> collectedProjects = new HashSet<EclipseProject>();

        for (ProjectConnection participant : participants) {
            EclipseProject rootProject = determineRootProject(participant.getModel(EclipseProject.class));
            DomainObjectSet<? extends EclipseProject> children = rootProject.getChildren();

            if (!children.isEmpty()) {
                traverseProjectHierarchy(rootProject, collectedProjects);
            } else {
                collectedProjects.add(rootProject);
            }
        }

        return collectedProjects;
    }

    private EclipseProject determineRootProject(EclipseProject eclipseProject) {
        if (eclipseProject.getParent() == null) {
            return eclipseProject;
        }

        return determineRootProject(eclipseProject.getParent());
    }

    private void traverseProjectHierarchy(EclipseProject parentProject, Set<EclipseProject> eclipseProjects) {
        DomainObjectSet<? extends EclipseProject> children = parentProject.getChildren();

        if (!children.isEmpty()) {
            for (EclipseProject childProject : children) {
                eclipseProjects.add(childProject);
                traverseProjectHierarchy(childProject, eclipseProjects);
            }
        }
    }

    // package-scoped class from Gradle core (needed copy/paste)
    private class BlockingResultHandler<T> implements ResultHandler<T> {
        private final BlockingQueue<Object> queue = new ArrayBlockingQueue<Object>(1);
        private final Class<T> resultType;
        private final Object NULL = new Object();

        public BlockingResultHandler(Class<T> resultType) {
            this.resultType = resultType;
        }

        public T getResult() {
            Object result;
            try {
                result = queue.take();
            } catch (InterruptedException e) {
                throw UncheckedException.throwAsUncheckedException(e);
            }

            if (result instanceof Throwable) {
                throw UncheckedException.throwAsUncheckedException(attachCallerThreadStackTrace((Throwable) result));
            }
            if (result == NULL) {
                return null;
            }
            return resultType.cast(result);
        }

        private Throwable attachCallerThreadStackTrace(Throwable failure) {
            List<StackTraceElement> adjusted = new ArrayList<StackTraceElement>();
            adjusted.addAll(Arrays.asList(failure.getStackTrace()));
            List<StackTraceElement> currentThreadStack = Arrays.asList(Thread.currentThread().getStackTrace());
            if (!currentThreadStack.isEmpty()) {
                adjusted.addAll(currentThreadStack.subList(2, currentThreadStack.size()));
            }
            failure.setStackTrace(adjusted.toArray(new StackTraceElement[adjusted.size()]));
            return failure;
        }

        public void onComplete(T result) {
            queue.add(result == null ? NULL : result);
        }

        public void onFailure(GradleConnectionException failure) {
            queue.add(failure);
        }
    }

    // package-scoped class from Gradle core (needed copy/paste)
    public class DefaultResultHandler<T> implements ResultHandlerVersion1<T> {
        private final ResultHandler<? super T> handler;

        public DefaultResultHandler(ResultHandler<? super T> handler) {
            this.handler = handler;
        }

        @Override
        public void onComplete(T result) {
            handler.onComplete(result);
        }

        @Override
        public void onFailure(Throwable failure) {
            if (failure instanceof InternalUnsupportedBuildArgumentException) {
                handler.onFailure(new UnsupportedBuildArgumentException(connectionFailureMessage(failure)
                        + "\n" + failure.getMessage(), failure));
            } else if (failure instanceof UnsupportedOperationConfigurationException) {
                handler.onFailure(new UnsupportedOperationConfigurationException(connectionFailureMessage(failure)
                        + "\n" + failure.getMessage(), failure.getCause()));
            } else if (failure instanceof GradleConnectionException) {
                handler.onFailure((GradleConnectionException) failure);
            } else if (failure instanceof InternalBuildCancelledException) {
                handler.onFailure(new BuildCancelledException(connectionFailureMessage(failure), failure.getCause()));
            } else if (failure instanceof InternalTestExecutionException) {
                handler.onFailure(new TestExecutionException(connectionFailureMessage(failure), failure.getCause()));
            } else if (failure instanceof BuildExceptionVersion1) {
                handler.onFailure(new BuildException(connectionFailureMessage(failure), failure.getCause()));
            } else if (failure instanceof ListenerNotificationException) {
                handler.onFailure(new ListenerFailedException(connectionFailureMessage(failure), ((ListenerNotificationException) failure).getCauses()));
            } else {
                handler.onFailure(new GradleConnectionException(connectionFailureMessage(failure), failure));
            }
        }

        private String connectionFailureMessage(Throwable failure) {
            String message = String.format("Could not fetch model of type '%s' using %s.", modelType.getSimpleName(), connection.getDisplayName());
            if (!(failure instanceof UnsupportedMethodException) && failure instanceof UnsupportedOperationException) {
                message += "\n" + Exceptions.INCOMPATIBLE_VERSION_HINT;
            }
            return message;
        }
    }
}
