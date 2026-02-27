package net.onefivefour.echolist.data.source

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
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import net.onefivefour.echolist.data.source.network.FileRemoteDataSourceImpl
import net.onefivefour.echolist.network.client.ConnectRpcClient

/**
 * Feature: proto-api-update, Property 3: FileRemoteDataSource RPC path correctness
 *
 * For any valid request, each FileRemoteDataSourceImpl method (createFolder, listFiles,
 * updateFolder, deleteFolder) should invoke ConnectRpcClient.call with the path
 * /file.v1.FileService/{MethodName}.
 *
 * **Validates: Requirements 2.3, 2.4, 2.5, 2.6**
 */
class FileRemoteDataSourcePropertyTest : FunSpec({

    // -- Capturing fake ConnectRpcClient --

    class CapturingClient : ConnectRpcClient {
        var capturedPath: String? = null

        override suspend fun <Req, Res> call(
            path: String,
            request: Req,
            requestSerializer: (Req) -> ByteArray,
            responseDeserializer: (ByteArray) -> Res
        ): Result<Res> {
            capturedPath = path
            val bytes = requestSerializer(request)
            return Result.success(responseDeserializer(bytes))
        }
    }

    // -- Property 3: RPC path correctness --

    test("Property 3: createFolder calls /file.v1.FileService/CreateFolder for any input") {
        checkAll(
            PropTestConfig(iterations = 100),
            Arb.string(0..100),
            Arb.string(0..100)
        ) { parentPath, name ->
            val client = CapturingClient()
            val dataSource = FileRemoteDataSourceImpl(client)

            runCatching {
                dataSource.createFolder(CreateFolderRequest(parent_path = parentPath, name = name))
            }

            client.capturedPath shouldBe "/file.v1.FileService/CreateFolder"
        }
    }

    test("Property 3: listFiles calls /file.v1.FileService/ListFiles for any input") {
        checkAll(
            PropTestConfig(iterations = 100),
            Arb.string(0..100)
        ) { parentPath ->
            val client = CapturingClient()
            val dataSource = FileRemoteDataSourceImpl(client)

            runCatching {
                dataSource.listFiles(ListFilesRequest(parent_path = parentPath))
            }

            client.capturedPath shouldBe "/file.v1.FileService/ListFiles"
        }
    }

    test("Property 3: updateFolder calls /file.v1.FileService/UpdateFolder for any input") {
        checkAll(
            PropTestConfig(iterations = 100),
            Arb.string(0..100),
            Arb.string(0..100)
        ) { folderPath, newName ->
            val client = CapturingClient()
            val dataSource = FileRemoteDataSourceImpl(client)

            runCatching {
                dataSource.updateFolder(UpdateFolderRequest(folder_path = folderPath, new_name = newName))
            }

            client.capturedPath shouldBe "/file.v1.FileService/UpdateFolder"
        }
    }

    test("Property 3: deleteFolder calls /file.v1.FileService/DeleteFolder for any input") {
        checkAll(
            PropTestConfig(iterations = 100),
            Arb.string(0..100)
        ) { folderPath ->
            val client = CapturingClient()
            val dataSource = FileRemoteDataSourceImpl(client)

            runCatching {
                dataSource.deleteFolder(DeleteFolderRequest(folder_path = folderPath))
            }

            client.capturedPath shouldBe "/file.v1.FileService/DeleteFolder"
        }
    }
})
