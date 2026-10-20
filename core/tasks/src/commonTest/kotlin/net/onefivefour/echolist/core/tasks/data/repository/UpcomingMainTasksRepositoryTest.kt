package net.onefivefour.echolist.core.tasks.data.repository

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.LocalDate
import net.onefivefour.echolist.core.networking.data.error.NetworkException
import net.onefivefour.echolist.core.tasks.data.source.network.FakeTaskListRemoteDataSource
import net.onefivefour.echolist.core.tasks.domain.model.UpcomingMainTask
import net.onefivefour.echolist.data.FakeDirectoryChangeNotifier
import tasks.v1.ListUpcomingMainTasksResponse
import tasks.v1.UpcomingMainTask as ProtoUpcomingMainTask

class UpcomingMainTasksRepositoryTest : FunSpec({
    test("sends the explicit inclusive cutoff and preserves server summaries without change events") {
        val source = FakeTaskListRemoteDataSource()
        source.listUpcomingMainTasksResult = Result.success(
            ListUpcomingMainTasksResponse(
                tasks = listOf(
                    ProtoUpcomingMainTask(
                        tasklist_id = "list-1", task_id = "later", title = "Later", due_date = "2028-03-01"
                    ),
                    ProtoUpcomingMainTask(
                        tasklist_id = "list-1", task_id = "cutoff", title = "Leap day", due_date = "2028-02-29"
                    )
                )
            )
        )
        val notifier = FakeDirectoryChangeNotifier()
        val repository = TaskListRepositoryImpl(source, Dispatchers.Unconfined, notifier)

        repository.listUpcomingMainTasks(LocalDate(2028, 2, 29)).getOrThrow() shouldBe listOf(
            UpcomingMainTask("later", "Later", LocalDate(2028, 3, 1), "list-1"),
            UpcomingMainTask("cutoff", "Leap day", LocalDate(2028, 2, 29), "list-1")
        )
        source.lastUpcomingRequest?.from_date shouldBe "2028-02-29"
        notifier.emissions shouldBe emptyList()
        source.lastListRequest shouldBe null
        source.lastGetRequest shouldBe null
    }

    test("empty results are successful and four-digit years are zero-padded") {
        listOf("0000-01-01", "0001-02-03", "9999-12-31").forEach { date ->
            val source = FakeTaskListRemoteDataSource()
            val repository = TaskListRepositoryImpl(source, Dispatchers.Unconfined, FakeDirectoryChangeNotifier())

            repository.listUpcomingMainTasks(LocalDate.parse(date)).getOrThrow() shouldBe emptyList()
            source.lastUpcomingRequest?.from_date shouldBe date
        }
    }

    test("rejects unsupported years before calling the remote source") {
        listOf(LocalDate(-1, 1, 1), LocalDate(10000, 1, 1)).forEach { date ->
            val source = FakeTaskListRemoteDataSource()
            val repository = TaskListRepositoryImpl(source, Dispatchers.Unconfined, FakeDirectoryChangeNotifier())

            repository.listUpcomingMainTasks(date).exceptionOrNull().shouldBeInstanceOf<IllegalArgumentException>()
            source.lastUpcomingRequest shouldBe null
        }
    }

    test("returns transport failures unchanged") {
        val source = FakeTaskListRemoteDataSource()
        val failure = NetworkException.ServerError(503, "Unavailable")
        source.listUpcomingMainTasksResult = Result.failure(failure)
        val notifier = FakeDirectoryChangeNotifier()
        val repository = TaskListRepositoryImpl(source, Dispatchers.Unconfined, notifier)

        repository.listUpcomingMainTasks(LocalDate(2026, 10, 20)).exceptionOrNull() shouldBe failure
        notifier.emissions shouldBe emptyList()
    }

    test("returns a mapping failure instead of a partial list") {
        val source = FakeTaskListRemoteDataSource()
        source.listUpcomingMainTasksResult = Result.success(
            ListUpcomingMainTasksResponse(
                tasks = listOf(
                    ProtoUpcomingMainTask(tasklist_id = "list-1", task_id = "valid", due_date = "2026-10-20"),
                    ProtoUpcomingMainTask(tasklist_id = "list-1", task_id = "invalid", due_date = "2026-02-30")
                )
            )
        )
        val repository = TaskListRepositoryImpl(source, Dispatchers.Unconfined, FakeDirectoryChangeNotifier())

        repository.listUpcomingMainTasks(LocalDate(2026, 10, 20))
            .exceptionOrNull().shouldBeInstanceOf<NetworkException.SerializationError>()
    }

    test("propagates cancellation instead of returning a failure result") {
        val source = FakeTaskListRemoteDataSource()
        val cancellation = CancellationException("Request cancelled")
        source.listUpcomingMainTasksResult = Result.failure(cancellation)
        val repository = TaskListRepositoryImpl(source, Dispatchers.Unconfined, FakeDirectoryChangeNotifier())

        shouldThrow<CancellationException> {
            repository.listUpcomingMainTasks(LocalDate(2026, 10, 20))
        } shouldBe cancellation
    }
})