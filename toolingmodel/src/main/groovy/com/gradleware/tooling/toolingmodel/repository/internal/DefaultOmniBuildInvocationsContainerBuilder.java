/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gradleware.tooling.toolingmodel.repository.internal;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;
import com.gradleware.tooling.toolingmodel.OmniBuildInvocations;
import com.gradleware.tooling.toolingmodel.OmniProjectTask;
import com.gradleware.tooling.toolingmodel.OmniTaskSelector;
import com.gradleware.tooling.toolingmodel.Path;
import org.gradle.tooling.model.DomainObjectSet;
import org.gradle.tooling.model.GradleProject;
import org.gradle.tooling.model.GradleTask;

import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

/**
 * Builds a {@code DefaultOmniBuildInvocationsContainer} from a given Gradle project.
 *
 * @author Etienne Studer
 */
public final class DefaultOmniBuildInvocationsContainerBuilder {

    @SuppressWarnings("RedundantStringConstructorCall")
    private static final String NULL_STRING = new String(); // ensure unique instance to use it as a null-string placeholder

    /**
     * Converts a {@link GradleProject} to a {@link DefaultOmniBuildInvocationsContainer}.
     *
     * @param project the Gradle project to convert
     * @param enforceAllTasksPublic if set to true {@code true}, all tasks should be made public
     * @return the build invocations container
     */
    public static DefaultOmniBuildInvocationsContainer build(GradleProject project, boolean enforceAllTasksPublic) {
        ImmutableMultimap<Path, OmniProjectTask> tasks = buildProjectTasksRecursively(project, ArrayListMultimap.<Path, OmniProjectTask>create(), enforceAllTasksPublic);
        ImmutableMultimap<Path, OmniTaskSelector> taskSelectors = buildTaskSelectorsRecursively(project, ArrayListMultimap.<Path, OmniTaskSelector>create(), enforceAllTasksPublic);
        ImmutableSortedMap<Path, OmniBuildInvocations> buildInvocationsPerProject = buildBuildInvocationsMapping(project, tasks, taskSelectors);
        return DefaultOmniBuildInvocationsContainer.from(buildInvocationsPerProject);
    }

    private static ImmutableSortedMap<Path, OmniBuildInvocations> buildBuildInvocationsMapping(GradleProject project,
                                                                                               Multimap<Path, OmniProjectTask> projectTasks,
                                                                                               Multimap<Path, OmniTaskSelector> taskSelectors) {
        Preconditions.checkState(taskSelectors.keySet().containsAll(projectTasks.keySet()), "Task selectors are always configured for all projects");

        // create mappings for all projects which contain tasks selectors (which covers at least those projects that contain project tasks)
        ImmutableSortedMap.Builder<Path, OmniBuildInvocations> mapping = ImmutableSortedMap.orderedBy(Path.Comparator.INSTANCE);
        for (Path projectPath : taskSelectors.keySet()) {
            ImmutableList<OmniProjectTask> projectTasksOfProject = ImmutableSortedSet.orderedBy(TaskComparator.INSTANCE).addAll(projectTasks.get(projectPath)).build().asList();
            ImmutableList<OmniTaskSelector> taskSelectorsOfProject = ImmutableSortedSet.orderedBy(TaskSelectorComparator.INSTANCE).addAll(taskSelectors.get(projectPath)).build().asList();
            mapping.put(projectPath, DefaultOmniBuildInvocations.from(projectTasksOfProject, taskSelectorsOfProject));
        }

        // create additional mappings for all those projects which do not contain any task selectors
        // this is the case if a project does not contain any tasks nor does any of its child projects
        // these additional mappings ensure the caller never gets back null for any project in the hierarchy
        Set<Path> projectPaths = Sets.newLinkedHashSet();
        collectProjectPathsRecursively(project, projectPaths);
        projectPaths.removeAll(taskSelectors.keySet());
        for (Path projectPath : projectPaths) {
            mapping.put(projectPath, DefaultOmniBuildInvocations.from(ImmutableList.<OmniProjectTask>of(), ImmutableList.<OmniTaskSelector>of()));
        }

        return mapping.build();
    }

    private static void collectProjectPathsRecursively(GradleProject project, Set<Path> projectPaths) {
        // add project's path
        projectPaths.add(Path.from(project.getPath()));

        // recurse into child projects and add their path
        for (GradleProject childProject : project.getChildren()) {
            collectProjectPathsRecursively(childProject, projectPaths);
        }
    }

    private static ImmutableMultimap<Path, OmniProjectTask> buildProjectTasksRecursively(GradleProject project, Multimap<Path, OmniProjectTask> tasksPerProject, boolean enforceAllTasksPublic) {
        // add tasks of the current project
        for (GradleTask task : project.getTasks()) {
            tasksPerProject.put(Path.from(project.getPath()), DefaultOmniProjectTask.from(task, enforceAllTasksPublic));
        }

        // recurse into child projects and add their tasks
        for (GradleProject childProject : project.getChildren()) {
            buildProjectTasksRecursively(childProject, tasksPerProject, enforceAllTasksPublic);
        }

        // return the tasks grouped by project path
        return ImmutableMultimap.copyOf(tasksPerProject);
    }

    @SuppressWarnings("StringEquality")
    private static ImmutableMultimap<Path, OmniTaskSelector> buildTaskSelectorsRecursively(GradleProject project, Multimap<Path, OmniTaskSelector> taskSelectorsPerProject, boolean enforceAllTasksPublic) {
        // add task selectors of the current project
        TreeBasedTable<String, Path, String> aggregatedTasksWithDescription = TreeBasedTable.create(Ordering.usingToString(), Path.Comparator.INSTANCE);
        Set<String> publicTasks = Sets.newLinkedHashSet();
        collectAllTasksRecursively(project, aggregatedTasksWithDescription, publicTasks, enforceAllTasksPublic);
        for (String selectorName : aggregatedTasksWithDescription.rowKeySet()) {
            SortedMap<Path, String> pathsAndDescriptions = aggregatedTasksWithDescription.row(selectorName);
            String description = pathsAndDescriptions.get(pathsAndDescriptions.firstKey()); // description from project task with smallest path
            SortedSet<Path> fqnTaskNames = ImmutableSortedSet.orderedBy(Path.Comparator.INSTANCE).addAll(pathsAndDescriptions.keySet()).build();

            OmniTaskSelector taskSelector = DefaultOmniTaskSelector.from(
                    selectorName,
                    description != NULL_STRING ? description : null,
                    Path.from(project.getPath()),
                    publicTasks.contains(selectorName),
                    fqnTaskNames);

            taskSelectorsPerProject.put(Path.from(project.getPath()), taskSelector);
        }

        // recurse into child projects and add their task selectors
        DomainObjectSet<? extends GradleProject> childProjects = project.getChildren();
        for (GradleProject childProject : childProjects) {
            buildTaskSelectorsRecursively(childProject, taskSelectorsPerProject, enforceAllTasksPublic);
        }

        // return the task selectors grouped by project path
        return ImmutableMultimap.copyOf(taskSelectorsPerProject);
    }

    private static void collectAllTasksRecursively(GradleProject project, Table<String, Path, String> tasksWithDescription, Collection<String> publicTasks, boolean enforceAllTasksPublic) {
        for (GradleTask task : project.getTasks()) {
            // convert to OmniProjectTask to have the version-specific logic and default-values applied
            OmniProjectTask projectTask = DefaultOmniProjectTask.from(task, enforceAllTasksPublic);

            // 1) store the path since the task selectors keep all the paths of the tasks they select
            // 2) store the description first by task name and then by path
            //      this allows to later fish out the description of the task whose name matches the selector name and
            //      whose path is the smallest for the given task name (the first entry of the table column)
            //      store null description as empty string to avoid that Guava chokes
            tasksWithDescription.put(projectTask.getName(), projectTask.getPath(), projectTask.getDescription() != null ? projectTask.getDescription() : NULL_STRING);

            // visible tasks are specified by Gradle as those that have a non-empty group
            if (projectTask.isPublic()) {
                publicTasks.add(task.getName());
            }
        }

        for (GradleProject childProject : project.getChildren()) {
            collectAllTasksRecursively(childProject, tasksWithDescription, publicTasks, enforceAllTasksPublic);
        }
    }

    /**
     * Singleton comparator to compare {@code OmniProjectTask} instances by their name.
     */
    private enum TaskComparator implements Comparator<OmniProjectTask> {

        INSTANCE;

        @Override
        public int compare(OmniProjectTask o1, OmniProjectTask o2) {
            return o1.getName().compareTo(o2.getName());
        }

    }

    /**
     * Singleton comparator to compare {@code OmniTaskSelector} instances by their name.
     */
    private enum TaskSelectorComparator implements Comparator<OmniTaskSelector> {

        INSTANCE;

        @Override
        public int compare(OmniTaskSelector o1, OmniTaskSelector o2) {
            return o1.getName().compareTo(o2.getName());
        }

    }

}
