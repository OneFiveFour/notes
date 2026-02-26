package net.onefivefour.echolist.data.source.network

import folder.v1.CreateFolderRequest
import folder.v1.CreateFolderResponse
import folder.v1.DeleteFolderRequest
import folder.v1.DeleteFolderResponse
import folder.v1.GetFolderRequest
import folder.v1.GetFolderResponse
import folder.v1.ListFoldersRequest
import folder.v1.ListFoldersResponse
import folder.v1.UpdateFolderRequest
import folder.v1.UpdateFolderResponse
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

class FolderNetworkDataSourceImplTest : FunSpec({

    // -- Generators --

    val arbProtoFolder = arbitrary {
        folder.v1.Folder(
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
        val response = CreateFolderResponse(folder = folder.v1.Folder(path = "/test/", name = "test"))
        val client = CapturingClient(CreateFolderResponse.ADAPTER.encode(response))
        val dataSource = FolderRemoteDataSourceImpl(client)

        dataSource.createFolder(CreateFolderRequest(parent_path = "", name = "test"))

        client.capturedPath shouldBe "/folder.v1.FolderService/CreateFolder"
    }

    test("createFolder deserializes response").config(invocations = 20) {
        checkAll(arbProtoFolder) { protoFolder ->
            val response = CreateFolderResponse(folder = protoFolder)
            val client = CapturingClient(CreateFolderResponse.ADAPTER.encode(response))
            val dataSource = FolderRemoteDataSourceImpl(client)

            val result = dataSource.createFolder(CreateFolderRequest(parent_path = "", name = "test"))

            result.folder shouldBe protoFolder
        }
    }

    test("createFolder propagates network errors") {
        val error = NetworkException.ServerError(500, "internal error")
        val client = FailingClient(error)
        val dataSource = FolderRemoteDataSourceImpl(client)

        val result = runCatching {
            dataSource.createFolder(CreateFolderRequest(parent_path = "", name = "n"))
        }

        result.isFailure shouldBe true
        result.exceptionOrNull().shouldBeInstanceOf<NetworkException.ServerError>()
    }

    // -- GetFolder --

    test("getFolder calls correct RPC path") {
        val response = GetFolderResponse(folder = folder.v1.Folder(path = "/test/", name = "test"))
        val client = CapturingClient(GetFolderResponse.ADAPTER.encode(response))
        val dataSource = FolderRemoteDataSourceImpl(client)

        dataSource.getFolder(GetFolderRequest(folder_path = "/test/"))

        client.capturedPath shouldBe "/folder.v1.FolderService/GetFolder"
    }

    // -- ListFolders --

    test("listFolders calls correct RPC path") {
        val response = ListFoldersResponse(folders = emptyList())
        val client = CapturingClient(ListFoldersResponse.ADAPTER.encode(response))
        val dataSource = FolderRemoteDataSourceImpl(client)

        dataSource.listFolders(ListFoldersRequest(parent_path = "/"))

        client.capturedPath shouldBe "/folder.v1.FolderService/ListFolders"
    }

    test("listFolders deserializes response").config(invocations = 20) {
        checkAll(Arb.list(arbProtoFolder, 0..5)) { folders ->
            val response = ListFoldersResponse(folders = folders)
            val client = CapturingClient(ListFoldersResponse.ADAPTER.encode(response))
            val dataSource = FolderRemoteDataSourceImpl(client)

            val result = dataSource.listFolders(ListFoldersRequest(parent_path = "/"))

            result.folders.size shouldBe folders.size
            result.folders.zip(folders).forEach { (actual, expected) ->
                actual.path shouldBe expected.path
                actual.name shouldBe expected.name
            }
        }
    }

    // -- UpdateFolder --

    test("updateFolder calls correct RPC path") {
        val response = UpdateFolderResponse(folder = folder.v1.Folder(path = "/new/", name = "new"))
        val client = CapturingClient(UpdateFolderResponse.ADAPTER.encode(response))
        val dataSource = FolderRemoteDataSourceImpl(client)

        dataSource.updateFolder(UpdateFolderRequest(folder_path = "/old/", new_name = "new"))

        client.capturedPath shouldBe "/folder.v1.FolderService/UpdateFolder"
    }

    test("updateFolder propagates network errors") {
        val error = NetworkException.ClientError(404, "not found")
        val client = FailingClient(error)
        val dataSource = FolderRemoteDataSourceImpl(client)

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
        val dataSource = FolderRemoteDataSourceImpl(client)

        dataSource.deleteFolder(DeleteFolderRequest(folder_path = "old/"))

        client.capturedPath shouldBe "/folder.v1.FolderService/DeleteFolder"
    }

    test("deleteFolder propagates network errors") {
        val error = NetworkException.NetworkError("connection refused")
        val client = FailingClient(error)
        val dataSource = FolderRemoteDataSourceImpl(client)

        val result = runCatching {
            dataSource.deleteFolder(DeleteFolderRequest(folder_path = "p/"))
        }

        result.isFailure shouldBe true
        result.exceptionOrNull().shouldBeInstanceOf<NetworkException.NetworkError>()
    }
})
