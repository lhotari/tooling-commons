package com.gradleware.tooling.toolingmodel.substitution;

import com.gradleware.tooling.toolingmodel.substitution.internal.DefaultGradleCompositeBuilder;
import org.gradle.tooling.ProjectConnection;

public abstract class GradleCompositeBuilder {

    /**
     * Creates a new composite builder.
     * The builder can add new participants for a composite with the method {@link #withParticipant(ProjectConnection)}.
     *
     * @return a new composite builder
     */
    public static GradleCompositeBuilder newComposite() {
        return new DefaultGradleCompositeBuilder();
    }

    /**
     * Adds a new participating project connection. The correlating project can be a single or multi-project build.
     *
     * @param participant participating project
     * @return this
     */
    protected abstract GradleCompositeBuilder withParticipant(ProjectConnection participant);

    /**
     * Builds the composite for the provided, participating project connections.
     *
     * @return the composite
     */
    protected abstract GradleCompositeBuild build();
}
