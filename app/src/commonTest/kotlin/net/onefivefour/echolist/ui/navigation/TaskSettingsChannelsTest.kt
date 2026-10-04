package net.onefivefour.echolist.ui.navigation

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import net.onefivefour.echolist.core.tasks.domain.model.TaskSettingsChanges

class TaskSettingsChannelsTest : FunSpec({
    test("two editors of the same task receive only their own settings") {
        runTest {
            val channels = TaskSettingsChannels()
            val first = channels.resultsFor("editor-a")
            val second = channels.resultsFor("editor-b")
            val changeA = TaskSettingsChanges("same-task", "2026-10-04", "", false)
            val changeB = changeA.copy(dueDate = "2026-10-05")
            channels.sinkFor("editor-a").emit(changeA)
            channels.sinkFor("editor-b").emit(changeB)
            first.first() shouldBe changeA
            second.first() shouldBe changeB
            channels.clearEditors()
        }
    }

    test("settings are queued until the owning editor starts collecting") {
        runTest {
            val channels = TaskSettingsChannels()
            val change = TaskSettingsChanges("draft", "2026-10-04", "FREQ=DAILY;INTERVAL=1")
            channels.sinkFor("editor").emit(change)
            channels.retainEditors(setOf("editor"))
            channels.resultsFor("editor").first() shouldBe change
            channels.clearEditors()
        }
    }

    test("a late permission result cannot reach a closed or newly opened editor") {
        runTest {
            val channels = TaskSettingsChannels()
            val oldResults = channels.resultsFor("editor")
            val oldSink = channels.sinkFor("editor")
            channels.clearEditors()
            oldSink.emit(TaskSettingsChanges("task", "", "", false))
            oldResults.toList() shouldBe emptyList()

            val newResults = channels.resultsFor("editor")
            val current = TaskSettingsChanges("task", "2026-10-06", "", true)
            channels.sinkFor("editor").emit(current)
            newResults.first() shouldBe current
            channels.clearEditors()
        }
    }
})