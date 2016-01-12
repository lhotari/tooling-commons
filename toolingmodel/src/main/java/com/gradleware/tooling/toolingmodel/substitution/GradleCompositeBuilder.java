package com.gradleware.tooling.toolingmodel.substitution;

import com.gradleware.tooling.toolingmodel.substitution.internal.DefaultGradleCompositeBuilder;
import org.gradle.tooling.ProjectConnection;

public abstract class GradleCompositeBuilder {
    public static GradleCompositeBuilder newComposite() {
        return new DefaultGradleCompositeBuilder();
    }

    protected abstract GradleCompositeBuilder withParticipant(ProjectConnection participant);
    protected abstract GradleCompositeBuild build();
}
