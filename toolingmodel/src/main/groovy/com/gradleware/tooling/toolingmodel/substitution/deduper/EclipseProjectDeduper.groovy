package com.gradleware.tooling.toolingmodel.substitution.deduper

import org.gradle.tooling.model.HierarchicalElement
import org.gradle.tooling.model.eclipse.EclipseProject

class EclipseProjectDeduper {
    Collection<EclipseProject> dedup(Collection<EclipseProject> projects) {
        doDedup(projects, { EclipseProject project, String name ->
            new NameOverridingEclipseProject(project: project, name: name)
        })
    }

    private Collection<HierarchicalElement> doDedup(Collection<HierarchicalElement> projects, Closure<HierarchicalElement> createDelegate) {
        Map<HierarchicalElement, String> updatedProjects = [:]
        new ProjectDeduper().dedupe(projects, { project ->
            new DeduplicationTarget(project: project,
                    moduleName: project.name,
                    updateModuleName: {
                        if (it != project.name) {
                            updatedProjects.put(project, it)
                        }
                    })
        })
        if (updatedProjects) {
            projects.collect {
                String updatedName = updatedProjects.get(it)
                if (updatedName) {
                    createDelegate(it, updatedName)
                } else {
                    it
                }
            }
        } else {
            projects
        }
    }
}

class NameOverridingEclipseProject implements EclipseProject {
    @Delegate
    EclipseProject project
    private String name

    String getName() {
        name
    }
}

