package com.gradleware.tooling.toolingmodel.substitution.internal;

import com.gradleware.tooling.toolingmodel.substitution.EclipseWorkspace;
import com.gradleware.tooling.toolingmodel.substitution.GradleCompositeBuild;
import org.gradle.tooling.GradleConnectionException;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.model.DomainObjectSet;
import org.gradle.tooling.model.eclipse.EclipseProject;

import java.util.HashSet;
import java.util.Set;

public class DefaultGradleCompositeBuild implements GradleCompositeBuild {

    private final Set<ProjectConnection> participants;

    public DefaultGradleCompositeBuild(Set<ProjectConnection> participants) {
        this.participants = participants;
    }

    /**
     * {@inheritDoc}
     *
     * The only allowed model type that can be requested is {@link com.gradleware.tooling.toolingmodel.substitution.EclipseWorkspace}.
     */
    @Override
    public <T> T getModel(Class<T> modelType) throws GradleConnectionException, IllegalStateException {
        if (!modelType.isInterface()) {
            throw new IllegalArgumentException(String.format("Cannot fetch a model of type '%s' as this type is not an interface.", modelType.getName()));
        }

        if (!modelType.equals(EclipseWorkspace.class)) {
            throw new IllegalArgumentException("The only supported model for a Gradle composite is EclipseWorkspace.class.");
        }


        Set<EclipseProject> openProjects = populateModel();
        return (T) new DefaultEclipseWorkspace(openProjects);
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
}
