package com.gradleware.tooling.toolingmodel.substitution;

import org.gradle.tooling.model.eclipse.EclipseProject;

import java.util.Set;

public interface EclipseWorkspace {

    /**
     * A flattened set of all projects in the Eclipse workspace.
     * These project models are fully configured, and may be expensive to calculate.
     * Note that not all projects necessarily share the same root.
     */
    Set<EclipseProject> getOpenProjects();
}
