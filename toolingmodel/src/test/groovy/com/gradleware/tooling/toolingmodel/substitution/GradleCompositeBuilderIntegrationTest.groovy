package com.gradleware.tooling.toolingmodel.substitution

import com.gradleware.tooling.junit.TestDirectoryProvider
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
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

    def "can create composite build with single participating project"() {
        given:
        File project1 = directoryProvider.createDir('project-1')
        createBuildFileWithDependency(project1, 'commons-lang:commons-lang:2.6')

        when:
        ProjectConnection project1Connection = createProjectConnection(project1)
        GradleCompositeBuild gradleCompositeBuild = createCompositeBuild(project1Connection)
        EclipseWorkspace eclipseWorkspace = gradleCompositeBuild.getModel(EclipseWorkspace)

        then:
        eclipseWorkspace.openProjects.size() == 1
        EclipseProject eclipseProject1 = eclipseWorkspace.openProjects[0]
        eclipseProject1.name == 'project-1'
        eclipseProject1.classpath.size() == 1
        def depModuleVersion = eclipseProject1.classpath[0].gradleModuleVersion
        depModuleVersion.group == 'commons-lang'
        depModuleVersion.name == 'commons-lang'
        depModuleVersion.version == '2.6'

        cleanup:
        project1Connection?.close()
    }

    def "can create composite build with multiple participating project"() {
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
        EclipseProject eclipseProject1 = eclipseWorkspace.openProjects.find { it.name == 'project-1'}
        eclipseProject1.classpath.size() == 1
        def depModuleVersion1 = eclipseProject1.classpath[0].gradleModuleVersion
        depModuleVersion1.group == 'commons-lang'
        depModuleVersion1.name == 'commons-lang'
        depModuleVersion1.version == '2.6'
        EclipseProject eclipseProject2 = eclipseWorkspace.openProjects.find { it.name == 'project-2'}
        eclipseProject2.classpath.size() == 1
        def depModuleVersion2 = eclipseProject2.classpath[0].gradleModuleVersion
        depModuleVersion2.group == 'log4j'
        depModuleVersion2.name == 'log4j'
        depModuleVersion2.version == '1.2.17'

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
        File buildFile = new File(projectDir, 'build.gradle')
        buildFile.createNewFile()
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
}
