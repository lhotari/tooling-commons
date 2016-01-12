package com.gradleware.tooling.toolingmodel.substitution.internal;

import com.gradleware.tooling.toolingmodel.substitution.GradleCompositeBuild;
import com.gradleware.tooling.toolingmodel.substitution.GradleCompositeBuilder;
import org.gradle.tooling.ProjectConnection;

import java.util.HashSet;
import java.util.Set;

public class DefaultGradleCompositeBuilder extends GradleCompositeBuilder {
    private final Set<ProjectConnection> participants = new HashSet<ProjectConnection>();

    public GradleCompositeBuilder withParticipant(ProjectConnection participant) {
        participants.add(participant);
        return this;
    }

    public GradleCompositeBuild build() {
        return new DefaultGradleCompositeBuild(participants);
    }
}
