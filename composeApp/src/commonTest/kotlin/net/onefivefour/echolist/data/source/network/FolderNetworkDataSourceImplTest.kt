package net.onefivefour.echolist.data.source.network

import folder.v1.CreateFolderRequest
import folder.v1.CreateFolderResponse
import folder.v1.DeleteFolderRequest
import folder.v1.DeleteFolderResponse
import folder.v1.RenameFolderRequest
import folder.v1.RenameFolderResponse
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
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

    val arbFolderEntry = arbitrary {
        folder.v1.FolderEntry(path = Arb.string(1..100).bind())
    }

    // -- Helpers --

    /**
     * A [ConnectRpcClient] that captures the path and returns a pre-built response.
     */
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

    /**
     * A [ConnectRpcClient] that always fails with the given exception.
     */
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

    test("createFolder calls correct RPC path").config(invocations = 20) {
        checkAll(Arb.list(arbFolderEntry, 0..5)) { entries ->
            val response = CreateFolderResponse(entries = entries)
            val client = CapturingClient(CreateFolderResponse.ADAPTER.encode(response))
            val dataSource = FolderNetworkDataSourceImpl(client)

            dataSource.createFolder(CreateFolderRequest(domain = "notes", parent_path = "", name = "test"))

            client.capturedPath shouldBe "/folder.v1.FolderService/CreateFolder"
        }
    }

    test("createFolder deserializes response entries").config(invocations = 20) {
        checkAll(Arb.list(arbFolderEntry, 0..5)) { entries ->
            val response = CreateFolderResponse(entries = entries)
            val client = CapturingClient(CreateFolderResponse.ADAPTER.encode(response))
            val dataSource = FolderNetworkDataSourceImpl(client)

            val result = dataSource.createFolder(
                CreateFolderRequest(domain = "notes", parent_path = "", name = "test")
            )

            result.entries.size shouldBe entries.size
            result.entries.zip(entries).forEach { (actual, expected) ->
                actual.path shouldBe expected.path
            }
        }
    }

    test("createFolder propagates network errors") {
        val error = NetworkException.ServerError(500, "internal error")
        val client = FailingClient(error)
        val dataSource = FolderNetworkDataSourceImpl(client)

        val result = runCatching {
            dataSource.createFolder(CreateFolderRequest(domain = "d", parent_path = "", name = "n"))
        }

        result.isFailure shouldBe true
        result.exceptionOrNull().shouldBeInstanceOf<NetworkException.ServerError>()
    }

    // -- RenameFolder --

    test("renameFolder calls correct RPC path").config(invocations = 20) {
        checkAll(Arb.list(arbFolderEntry, 0..5)) { entries ->
            val response = RenameFolderResponse(entries = entries)
            val client = CapturingClient(RenameFolderResponse.ADAPTER.encode(response))
            val dataSource = FolderNetworkDataSourceImpl(client)

            dataSource.renameFolder(
                RenameFolderRequest(domain = "notes", folder_path = "old/", new_name = "new")
            )

            client.capturedPath shouldBe "/folder.v1.FolderService/RenameFolder"
        }
    }

    test("renameFolder deserializes response entries").config(invocations = 20) {
        checkAll(Arb.list(arbFolderEntry, 0..5)) { entries ->
            val response = RenameFolderResponse(entries = entries)
            val client = CapturingClient(RenameFolderResponse.ADAPTER.encode(response))
            val dataSource = FolderNetworkDataSourceImpl(client)

            val result = dataSource.renameFolder(
                RenameFolderRequest(domain = "notes", folder_path = "old/", new_name = "new")
            )

            result.entries.size shouldBe entries.size
            result.entries.zip(entries).forEach { (actual, expected) ->
                actual.path shouldBe expected.path
            }
        }
    }

    test("renameFolder propagates network errors") {
        val error = NetworkException.ClientError(404, "not found")
        val client = FailingClient(error)
        val dataSource = FolderNetworkDataSourceImpl(client)

        val result = runCatching {
            dataSource.renameFolder(
                RenameFolderRequest(domain = "d", folder_path = "p/", new_name = "n")
            )
        }

        result.isFailure shouldBe true
        result.exceptionOrNull().shouldBeInstanceOf<NetworkException.ClientError>()
    }

    // -- DeleteFolder --

    test("deleteFolder calls correct RPC path").config(invocations = 20) {
        checkAll(Arb.list(arbFolderEntry, 0..5)) { entries ->
            val response = DeleteFolderResponse(entries = entries)
            val client = CapturingClient(DeleteFolderResponse.ADAPTER.encode(response))
            val dataSource = FolderNetworkDataSourceImpl(client)

            dataSource.deleteFolder(DeleteFolderRequest(domain = "notes", folder_path = "old/"))

            client.capturedPath shouldBe "/folder.v1.FolderService/DeleteFolder"
        }
    }

    test("deleteFolder deserializes response entries").config(invocations = 20) {
        checkAll(Arb.list(arbFolderEntry, 0..5)) { entries ->
            val response = DeleteFolderResponse(entries = entries)
            val client = CapturingClient(DeleteFolderResponse.ADAPTER.encode(response))
            val dataSource = FolderNetworkDataSourceImpl(client)

            val result = dataSource.deleteFolder(
                DeleteFolderRequest(domain = "notes", folder_path = "old/")
            )

            result.entries.size shouldBe entries.size
            result.entries.zip(entries).forEach { (actual, expected) ->
                actual.path shouldBe expected.path
            }
        }
    }

    test("deleteFolder propagates network errors") {
        val error = NetworkException.NetworkError("connection refused")
        val client = FailingClient(error)
        val dataSource = FolderNetworkDataSourceImpl(client)

        val result = runCatching {
            dataSource.deleteFolder(DeleteFolderRequest(domain = "d", folder_path = "p/"))
        }

        result.isFailure shouldBe true
        result.exceptionOrNull().shouldBeInstanceOf<NetworkException.NetworkError>()
    }
})
