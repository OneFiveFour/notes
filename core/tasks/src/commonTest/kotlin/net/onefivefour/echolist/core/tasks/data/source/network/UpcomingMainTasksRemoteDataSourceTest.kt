package net.onefivefour.echolist.core.tasks.data.source.network

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import net.onefivefour.echolist.core.networking.data.client.ConnectRpcClient
import net.onefivefour.echolist.core.networking.data.error.NetworkException
import tasks.v1.ListUpcomingMainTasksRequest
import tasks.v1.ListUpcomingMainTasksResponse
import tasks.v1.UpcomingMainTask

class UpcomingMainTasksRemoteDataSourceTest : FunSpec({
    test("uses the upcoming RPC path and real Wire request and response adapters") {
        val response = ListUpcomingMainTasksResponse(
            tasks = listOf(
                UpcomingMainTask(
                    tasklist_id = "list-1",
                    task_id = "task-1",
                    title = "Übermorgen",
                    due_date = "2026-10-22"
                )
            )
        )
        val client = RecordingUpcomingClient(Result.success(ListUpcomingMainTasksResponse.ADAPTER.encode(response)))
        val source = TaskListRemoteDataSourceImpl(client)

        source.listUpcomingMainTasks(ListUpcomingMainTasksRequest(from_date = "2026-10-20")) shouldBe response
        client.path shouldBe "/tasks.v1.TaskListService/ListUpcomingMainTasks"
        ListUpcomingMainTasksRequest.ADAPTER.decode(requireNotNull(client.requestBytes)).from_date shouldBe "2026-10-20"
        client.calls shouldBe 1
    }

    test("decodes an empty protobuf response as an empty list") {
        val source = TaskListRemoteDataSourceImpl(RecordingUpcomingClient(Result.success(byteArrayOf())))

        source.listUpcomingMainTasks(ListUpcomingMainTasksRequest(from_date = "2026-10-20")) shouldBe
            ListUpcomingMainTasksResponse()
    }

    test("propagates client failures") {
        val failure = NetworkException.ClientError(401, "Unauthorized")
        val source = TaskListRemoteDataSourceImpl(RecordingUpcomingClient(Result.failure(failure)))

        shouldThrow<NetworkException.ClientError> {
            source.listUpcomingMainTasks(ListUpcomingMainTasksRequest(from_date = "2026-10-20"))
        } shouldBe failure
    }
})

private class RecordingUpcomingClient(private val response: Result<ByteArray>) : ConnectRpcClient {
    var path: String? = null
    var requestBytes: ByteArray? = null
    var calls = 0

    override suspend fun <Req, Res> call(
        path: String,
        request: Req,
        requestSerializer: (Req) -> ByteArray,
        responseDeserializer: (ByteArray) -> Res
    ): Result<Res> {
        this.path = path
        requestBytes = requestSerializer(request)
        calls++
        return response.map { responseDeserializer(it) }
    }
}