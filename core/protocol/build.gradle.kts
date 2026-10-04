plugins {
    id("echolist.kmp.library")
    alias(libs.plugins.wire)
}
kotlin.sourceSets.getByName("commonMain").dependencies { api(libs.wire.runtime) }
wire {
    kotlin {}
    sourcePath { srcDir("${rootProject.projectDir}/proto") }
    prune("notes.v1.NoteService")
    prune("auth.v1.AuthService")
    prune("file.v1.FileService")
    prune("tasks.v1.TaskListService")
}
