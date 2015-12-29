package com.gradleware.tooling.toolingmodel.substitution;

public interface GradleWorkspace extends ModelProvider {

    /**
     * Adds a project to the workspace based on identity.
     *
     * @param projectIdentity Project identity
     */
    void addProject(ProjectIdentity projectIdentity);

    /**
     * Removes a project from the workspace based on identity.
     *
     * @param projectIdentity Project identity
     */
    void removeProject(ProjectIdentity projectIdentity);
}