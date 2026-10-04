import org.gradle.api.artifacts.ProjectDependency

plugins { base }

val architecture = tasks.register<VerifyArchitecture>("verifyArchitecture") {
    group = "verification"
    description = "Checks the project graph and Clean Architecture source boundaries."
    repository.set(layout.projectDirectory)
    sources.from(fileTree("app/src") { include("*Main/**/*.kt") })
    sources.from(fileTree("apps") { include("*/src/*Main/**/*.kt", "*/src/main/**/*.kt") })
    sources.from(fileTree("core") { include("*/src/*Main/**/*.kt") })
    sources.from(fileTree("features") { include("*/src/*Main/**/*.kt") })
}

gradle.projectsEvaluated {
    val graph = rootProject.subprojects.filter { it.buildFile.exists() }.associate { module ->
        module.path to module.configurations.flatMap { configuration ->
            configuration.dependencies.withType<ProjectDependency>().map { it.path }
        // AGP adds a self-project dependency for its test components.
        }.filter { it != module.path }.distinct().sorted()
    }
    architecture.configure { dependencies.set(graph) }
}

tasks.named("check") { dependsOn(architecture) }
