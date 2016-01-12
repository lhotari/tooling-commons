package com.gradleware.tooling.toolingmodel.substitution.internal;

import com.gradleware.tooling.toolingmodel.substitution.EclipseWorkspace;
import org.gradle.tooling.model.eclipse.EclipseProject;

import java.util.Set;


public class DefaultEclipseWorkspace implements EclipseWorkspace {

    private final Set<EclipseProject> openProjects;

    public DefaultEclipseWorkspace(Set<EclipseProject> openProjects) {
        this.openProjects = openProjects;
    }

    @Override
    public Set<EclipseProject> getOpenProjects() {
        return openProjects;
    }
}
