package com.gradleware.tooling.toolingmodel.substitution.internal;

import com.gradleware.tooling.toolingmodel.substitution.EclipseWorkspace;
import com.gradleware.tooling.toolingmodel.substitution.ProjectIdentity;
import org.gradle.tooling.model.eclipse.EclipseProject;

import java.util.Set;

public class DefaultEclipseWorkspace implements EclipseWorkspace {

    private final Set<ProjectIdentity> projectIdentities;

    public DefaultEclipseWorkspace(Set<ProjectIdentity> projectIdentities) {
        this.projectIdentities = projectIdentities;
    }

    @Override
    public Set<EclipseProject> getProjects() {
        // create project from IDs with EclipseModelBuilder
        return null;
    }
}
