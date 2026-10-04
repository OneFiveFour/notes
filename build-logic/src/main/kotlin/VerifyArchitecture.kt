import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

abstract class VerifyArchitecture : DefaultTask() {
    @get:Input
    abstract val dependencies: MapProperty<String, List<String>>

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sources: ConfigurableFileCollection

    @get:Internal
    abstract val repository: DirectoryProperty

    @TaskAction
    fun verify() {
        val failures = mutableListOf<String>()
        val graph = dependencies.get()
        graph.forEach { (module, targets) ->
            targets.forEach { target ->
                val allowed = when {
                    module.startsWith(":features:") -> target.startsWith(":core:")
                    module.startsWith(":core:") -> target.startsWith(":core:")
                    module == ":app" -> target.startsWith(":core:") || target.startsWith(":features:")
                    module.startsWith(":apps:") -> target == ":app" || target.startsWith(":core:")
                    else -> false
                }
                if (!allowed) failures += "Forbidden project dependency: $module -> $target"
            }
        }
        val visited = mutableSetOf<String>()
        val visiting = linkedSetOf<String>()
        fun visit(module: String) {
            if (module in visiting) {
                failures += "Dependency cycle: ${(visiting + module).joinToString(" -> ")}"
                return
            }
            if (!visited.add(module)) return
            visiting += module
            graph[module].orEmpty().forEach(::visit)
            visiting -= module
        }
        graph.keys.forEach(::visit)

        val importPattern = Regex("(?m)^import\\s+([^\\s;]+)")
        val forbiddenDomain = Regex("\\.(data|ui|di)\\.|^(android\\.|androidx\\.|org\\.koin\\.|io\\.ktor\\.|app\\.cash\\.sqldelight\\.)|\\.v1\\.")
        sources.files.sortedBy { it.path }.forEach { file ->
            val path = file.relativeTo(repository.get().asFile).invariantSeparatorsPath
            val text = file.readText()
            val imports = importPattern.findAll(text).map { it.groupValues[1] }.toList()
            val feature = path.takeIf { it.startsWith("features/") }?.split('/')?.get(1)
            imports.forEach { name ->
                if ("/domain/" in path && forbiddenDomain.containsMatchIn(name)) {
                    failures += "$path: domain imports implementation $name"
                }
                if ("/ui/" in path && name.startsWith("net.onefivefour.echolist.") && ".data." in name) {
                    failures += "$path: UI imports data implementation $name"
                }
                if (feature != null && name.startsWith("net.onefivefour.echolist.feature.") &&
                    !name.startsWith("net.onefivefour.echolist.feature.$feature.")) {
                    failures += "$path: imports another feature $name"
                }
                if (path.startsWith("core/") && name.startsWith("net.onefivefour.echolist.") &&
                    !name.startsWith("net.onefivefour.echolist.core.") &&
                    !name.startsWith("net.onefivefour.echolist.cache.") &&
                    name !in legacyNotificationReceivers) {
                    failures += "$path: core imports application code $name"
                }
                if (path.startsWith("app/") && name.startsWith("net.onefivefour.echolist.feature.") &&
                    !Regex("net\\.onefivefour\\.echolist\\.feature\\.[^.]+\\.(di\\.[^.]+Module|ui\\.[^.]+Feature)").matches(name)) {
                    failures += "$path: app must use feature entrypoints: $name"
                }
            }
        }
        if (failures.isNotEmpty()) throw GradleException(failures.joinToString("\n"))
        logger.lifecycle("Architecture verified: ${graph.size} projects, ${sources.files.size} production Kotlin files.")
    }

    companion object {
        // Existing Android PendingIntents address these stable receiver class names.
        private val legacyNotificationReceivers = setOf(
            "net.onefivefour.echolist.data.notification.TaskDoneReceiver",
            "net.onefivefour.echolist.data.notification.TaskReminderReceiver"
        )
    }
}
