package net.onefivefour.echolist.data.source.network

import `file`.v1.CreateFolderRequest
import `file`.v1.CreateFolderResponse
import `file`.v1.DeleteFolderRequest
import `file`.v1.DeleteFolderResponse
import `file`.v1.ListFilesRequest
import `file`.v1.ListFilesResponse
import `file`.v1.UpdateFolderRequest
import `file`.v1.UpdateFolderResponse
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import net.onefivefour.echolist.network.client.ConnectRpcClient
import net.onefivefour.echolist.network.error.NetworkException

class FileNetworkDataSourceImplTest : FunSpec({

    // -- Generators --

    val arbProtoFolder = arbitrary {
        `file`.v1.Folder(
            path = Arb.string(1..100).bind(),
            name = Arb.string(1..100).bind()
        )
    }

    // -- Helpers --

    class CapturingClient(
        private val responseBytes: ByteArray
    ) : ConnectRpcClient {
        var capturedPath: String? = null

        override suspend fun <Req, Res> call(
            path: String,
            request: Req,
            requestSerializer: (Req) -> ByteArray,
            responseDeserializer: (ByteArray) -> Res
        ): Result<Res> {
            capturedPath = path
            return Result.success(responseDeserializer(responseBytes))
        }
    }

    class FailingClient(
        private val exception: Exception
    ) : ConnectRpcClient {
        override suspend fun <Req, Res> call(
            path: String,
            request: Req,
            requestSerializer: (Req) -> ByteArray,
            responseDeserializer: (ByteArray) -> Res
        ): Result<Res> = Result.failure(exception)
    }


    // -- CreateFolder --

    test("createFolder calls correct RPC path") {
        val response = CreateFolderResponse(folder = `file`.v1.Folder(path = "/test/", name = "test"))
        val client = CapturingClient(CreateFolderResponse.ADAPTER.encode(response))
        val dataSource = FileRemoteDataSourceImpl(client)

        dataSource.createFolder(CreateFolderRequest(parent_path = "", name = "test"))

        client.capturedPath shouldBe "/file.v1.FileService/CreateFolder"
    }

    test("createFolder deserializes response").config(invocations = 20) {
        checkAll(arbProtoFolder) { protoFolder ->
            val response = CreateFolderResponse(folder = protoFolder)
            val client = CapturingClient(CreateFolderResponse.ADAPTER.encode(response))
            val dataSource = FileRemoteDataSourceImpl(client)

            val result = dataSource.createFolder(CreateFolderRequest(parent_path = "", name = "test"))

            result.folder shouldBe protoFolder
        }
    }

    test("createFolder propagates network errors") {
        val error = NetworkException.ServerError(500, "internal error")
        val client = FailingClient(error)
        val dataSource = FileRemoteDataSourceImpl(client)

        val result = runCatching {
            dataSource.createFolder(CreateFolderRequest(parent_path = "", name = "n"))
        }

        result.isFailure shouldBe true
        result.exceptionOrNull().shouldBeInstanceOf<NetworkException.ServerError>()
    }

    // -- ListFiles --

    test("listFiles calls correct RPC path") {
        val response = ListFilesResponse(entries = emptyList())
        val client = CapturingClient(ListFilesResponse.ADAPTER.encode(response))
        val dataSource = FileRemoteDataSourceImpl(client)

        dataSource.listFiles(ListFilesRequest(parent_path = "/"))

        client.capturedPath shouldBe "/file.v1.FileService/ListFiles"
    }

    test("listFiles deserializes response").config(invocations = 20) {
        checkAll(Arb.list(Arb.string(1..100), 0..5)) { entries ->
            val response = ListFilesResponse(entries = entries)
            val client = CapturingClient(ListFilesResponse.ADAPTER.encode(response))
            val dataSource = FileRemoteDataSourceImpl(client)

            val result = dataSource.listFiles(ListFilesRequest(parent_path = "/"))

            result.entries.size shouldBe entries.size
            result.entries shouldBe entries
        }
    }

    // -- UpdateFolder --

    test("updateFolder calls correct RPC path") {
        val response = UpdateFolderResponse(folder = `file`.v1.Folder(path = "/new/", name = "new"))
        val client = CapturingClient(UpdateFolderResponse.ADAPTER.encode(response))
        val dataSource = FileRemoteDataSourceImpl(client)

        dataSource.updateFolder(UpdateFolderRequest(folder_path = "/old/", new_name = "new"))

        client.capturedPath shouldBe "/file.v1.FileService/UpdateFolder"
    }

    test("updateFolder propagates network errors") {
        val error = NetworkException.ClientError(404, "not found")
        val client = FailingClient(error)
        val dataSource = FileRemoteDataSourceImpl(client)

        val result = runCatching {
            dataSource.updateFolder(UpdateFolderRequest(folder_path = "p/", new_name = "n"))
        }

        result.isFailure shouldBe true
        result.exceptionOrNull().shouldBeInstanceOf<NetworkException.ClientError>()
    }

    // -- DeleteFolder --

    test("deleteFolder calls correct RPC path") {
        val response = DeleteFolderResponse()
        val client = CapturingClient(DeleteFolderResponse.ADAPTER.encode(response))
        val dataSource = FileRemoteDataSourceImpl(client)

        dataSource.deleteFolder(DeleteFolderRequest(folder_path = "old/"))

        client.capturedPath shouldBe "/file.v1.FileService/DeleteFolder"
    }

    test("deleteFolder propagates network errors") {
        val error = NetworkException.NetworkError("connection refused")
        val client = FailingClient(error)
        val dataSource = FileRemoteDataSourceImpl(client)

        val result = runCatching {
            dataSource.deleteFolder(DeleteFolderRequest(folder_path = "p/"))
        }

        result.isFailure shouldBe true
        result.exceptionOrNull().shouldBeInstanceOf<NetworkException.NetworkError>()
    }
})
