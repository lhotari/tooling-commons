package com.gradleware.tooling.toolingmodel.substitution

import com.gradleware.tooling.junit.TestDirectoryProvider
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import org.gradle.tooling.model.GradleModuleVersion
import org.gradle.tooling.model.eclipse.EclipseProject
import org.junit.Rule
import spock.lang.Specification

class GradleCompositeBuilderIntegrationTest extends Specification {

    @Rule
    TestDirectoryProvider directoryProvider = new TestDirectoryProvider()

    def "cannot request model that is not an interface"() {
        given:
        File project1 = directoryProvider.createDir('project-1')
        createBuildFileWithDependency(project1, 'commons-lang:commons-lang:2.6')

        when:
        ProjectConnection project1Connection = createProjectConnection(project1)
        GradleCompositeBuild gradleCompositeBuild = createCompositeBuild(project1Connection)
        gradleCompositeBuild.getModel(String)

        then:
        Throwable t = thrown(IllegalArgumentException)
        t.message == "Cannot fetch a model of type 'java.lang.String' as this type is not an interface."

        cleanup:
        project1Connection?.close()
    }

    def "cannot request model for unknown model"() {
        given:
        File project1 = directoryProvider.createDir('project-1')
        createBuildFileWithDependency(project1, 'commons-lang:commons-lang:2.6')

        when:
        ProjectConnection project1Connection = createProjectConnection(project1)
        GradleCompositeBuild gradleCompositeBuild = createCompositeBuild(project1Connection)
        gradleCompositeBuild.getModel(List)

        then:
        Throwable t = thrown(IllegalArgumentException)
        t.message == "The only supported model for a Gradle composite is EclipseWorkspace.class."

        cleanup:
        project1Connection?.close()
    }

    def "can create composite with single participating project"() {
        given:
        File project1 = directoryProvider.createDir('project-1')
        createBuildFileWithDependency(project1, 'commons-lang:commons-lang:2.6')

        when:
        ProjectConnection project1Connection = createProjectConnection(project1)
        GradleCompositeBuild gradleCompositeBuild = createCompositeBuild(project1Connection)
        EclipseWorkspace eclipseWorkspace = gradleCompositeBuild.getModel(EclipseWorkspace)

        then:
        eclipseWorkspace.openProjects.size() == 1
        assertExternalDependencyForProject(eclipseWorkspace, 'project-1', new ExternalDependency(group: 'commons-lang', name: 'commons-lang', version: '2.6'))

        cleanup:
        project1Connection?.close()
    }

    def "can create composite with multiple participating project"() {
        given:
        File project1 = directoryProvider.createDir('project-1')
        createBuildFileWithDependency(project1, 'commons-lang:commons-lang:2.6')
        File project2 = directoryProvider.createDir('project-2')
        createBuildFileWithDependency(project2, 'log4j:log4j:1.2.17')

        when:
        ProjectConnection project1Connection = createProjectConnection(project1)
        ProjectConnection project2Connection = createProjectConnection(project2)
        GradleCompositeBuild gradleCompositeBuild = createCompositeBuild(project1Connection, project2Connection)
        EclipseWorkspace eclipseWorkspace = gradleCompositeBuild.getModel(EclipseWorkspace)

        then:
        eclipseWorkspace.openProjects.size() == 2
        assertExternalDependencyForProject(eclipseWorkspace, 'project-1', new ExternalDependency(group: 'commons-lang', name: 'commons-lang', version: '2.6'))
        assertExternalDependencyForProject(eclipseWorkspace, 'project-2', new ExternalDependency(group: 'log4j', name: 'log4j', version: '1.2.17'))

        cleanup:
        project1Connection?.close()
        project2Connection?.close()
    }

    def "can create composite with single participating multi-project build"() {
        given:
        File rootProjectDir = directoryProvider.createDir('multi-project-1')
        createBuildFileWithDependency(new File(rootProjectDir, 'sub-1'), 'commons-lang:commons-lang:2.6')
        createBuildFileWithDependency(new File(rootProjectDir, 'sub-2'), 'log4j:log4j:1.2.17')
        createSettingsFile(rootProjectDir, ['sub-1', 'sub-2'])

        when:
        ProjectConnection project1Connection = createProjectConnection(rootProjectDir)
        GradleCompositeBuild gradleCompositeBuild = createCompositeBuild(project1Connection)
        EclipseWorkspace eclipseWorkspace = gradleCompositeBuild.getModel(EclipseWorkspace)

        then:
        eclipseWorkspace.openProjects.size() == 2
        assertExternalDependencyForProject(eclipseWorkspace, 'sub-1', new ExternalDependency(group: 'commons-lang', name: 'commons-lang', version: '2.6'))
        assertExternalDependencyForProject(eclipseWorkspace, 'sub-2', new ExternalDependency(group: 'log4j', name: 'log4j', version: '1.2.17'))

        cleanup:
        project1Connection?.close()
    }

    def "can create composite with multiple participating multi-project builds"() {
        given:
        File rootProjectDir1 = directoryProvider.createDir('multi-project-1')
        createBuildFileWithDependency(new File(rootProjectDir1, 'sub-a'), 'commons-lang:commons-lang:2.6')
        createBuildFileWithDependency(new File(rootProjectDir1, 'sub-b'), 'log4j:log4j:1.2.17')
        createSettingsFile(rootProjectDir1, ['sub-a', 'sub-b'])

        File rootProjectDir2 = directoryProvider.createDir('multi-project-2')
        createBuildFileWithDependency(new File(rootProjectDir2, 'sub-1'), 'commons-math:commons-math:1.2')
        createBuildFileWithDependency(new File(rootProjectDir2, 'sub-2'), 'commons-codec:commons-codec:1.10')
        createSettingsFile(rootProjectDir2, ['sub-1', 'sub-2'])

        when:
        ProjectConnection project1Connection = createProjectConnection(rootProjectDir1)
        ProjectConnection project2Connection = createProjectConnection(rootProjectDir2)
        GradleCompositeBuild gradleCompositeBuild = createCompositeBuild(project1Connection, project2Connection)
        EclipseWorkspace eclipseWorkspace = gradleCompositeBuild.getModel(EclipseWorkspace)

        then:
        eclipseWorkspace.openProjects.size() == 4
        assertExternalDependencyForProject(eclipseWorkspace, 'sub-a', new ExternalDependency(group: 'commons-lang', name: 'commons-lang', version: '2.6'))
        assertExternalDependencyForProject(eclipseWorkspace, 'sub-b', new ExternalDependency(group: 'log4j', name: 'log4j', version: '1.2.17'))
        assertExternalDependencyForProject(eclipseWorkspace, 'sub-1', new ExternalDependency(group: 'commons-math', name: 'commons-math', version: '1.2'))
        assertExternalDependencyForProject(eclipseWorkspace, 'sub-2', new ExternalDependency(group: 'commons-codec', name: 'commons-codec', version: '1.10'))

        cleanup:
        project1Connection?.close()
        project2Connection?.close()
    }

    private ProjectConnection createProjectConnection(File projectDir) {
        GradleConnector.newConnector().forProjectDirectory(projectDir).connect()
    }

    private GradleCompositeBuild createCompositeBuild(ProjectConnection... participants) {
        GradleCompositeBuilder compositeBuilder = GradleCompositeBuilder.newComposite()

        participants.each {
            compositeBuilder.withParticipant(it)
        }

        compositeBuilder.build()
    }

    private void createBuildFileWithDependency(File projectDir, String coordinates) {
        createDir(projectDir)
        File buildFile = new File(projectDir, 'build.gradle')
        createFile(buildFile)

        buildFile << """
            apply plugin: 'java'

            repositories {
                mavenCentral()
            }

            dependencies {
                compile '$coordinates'
            }
        """
    }

    private void createSettingsFile(File projectDir, List<String> projectPaths) {
        File settingsFile = new File(projectDir, 'settings.gradle')
        createFile(settingsFile)
        String includes = projectPaths.collect { "'$it'" }.join(', ')
        settingsFile << "include $includes"
    }

    private void createDir(File dir) {
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IllegalStateException("Failed to create directory $dir")
        }
    }

    private void createFile(File file) {
        if (!file.exists() && !file.createNewFile()) {
            throw new IllegalStateException("Failed to create file $file")
        }
    }

    private void assertExternalDependencyForProject(EclipseWorkspace eclipseWorkspace, String projectName, ExternalDependency externalDependency) {
        EclipseProject eclipseProject = eclipseWorkspace.openProjects.find { it.name == projectName }
        assert eclipseProject
        assert eclipseProject.classpath.size() == 1
        GradleModuleVersion depModuleVersion = eclipseProject.classpath[0].gradleModuleVersion
        assert depModuleVersion.group == externalDependency.group
        assert depModuleVersion.name == externalDependency.name
        assert depModuleVersion.version == externalDependency.version
    }

    private static class ExternalDependency {
        String group
        String name
        String version
    }
}
