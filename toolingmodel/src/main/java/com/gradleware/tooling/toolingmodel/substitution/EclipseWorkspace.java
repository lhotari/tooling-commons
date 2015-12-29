package com.gradleware.tooling.toolingmodel.substitution;

import org.gradle.tooling.model.eclipse.EclipseProject;

import java.util.Set;

public interface EclipseWorkspace {
    Set<EclipseProject> getProjects();
}
