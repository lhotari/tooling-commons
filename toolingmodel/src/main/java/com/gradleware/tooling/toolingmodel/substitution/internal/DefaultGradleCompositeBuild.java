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

    @Override
    public <T> T getModel(Class<T> modelType) throws GradleConnectionException, IllegalStateException {
        if (!modelType.isInterface()) {
            throw new IllegalArgumentException(String.format("Cannot fetch a model of type '%s' as this type is not an interface.", modelType.getName()));
        }

        if (!modelType.equals(EclipseWorkspace.class)) {
            throw new IllegalArgumentException("The only supported model for a Gradle composite is EclipseWorkspace.class.");
        }


        Set<EclipseProject> openProjects = populate();
        return (T) new DefaultEclipseWorkspace(openProjects);
    }

    private Set<EclipseProject> populate() {
        Set<EclipseProject> collectedProjects = new HashSet<EclipseProject>();

        for (ProjectConnection participant : participants) {
            EclipseProject eclipseProject = participant.getModel(EclipseProject.class);
            DomainObjectSet<? extends EclipseProject> children = eclipseProject.getChildren();

            if (!children.isEmpty()) {
                traverseProjects(eclipseProject, collectedProjects);
            } else {
                collectedProjects.add(eclipseProject);
            }
        }

        return collectedProjects;
    }

    private void traverseProjects(EclipseProject parentProject, Set<EclipseProject> eclipseProjects) {
        DomainObjectSet<? extends EclipseProject> children = parentProject.getChildren();

        if (!children.isEmpty()) {
            for (EclipseProject childProject : children) {
                eclipseProjects.add(childProject);
                traverseProjects(childProject, eclipseProjects);
            }
        }
    }
}
