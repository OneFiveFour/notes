package net.onefivefour.echolist.data.repository

import `file`.v1.CreateFolderResponse
import `file`.v1.DeleteFolderResponse
import `file`.v1.UpdateFolderResponse
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.Dispatchers
import net.onefivefour.echolist.data.models.CreateFolderParams
import net.onefivefour.echolist.data.models.DeleteFolderParams
import net.onefivefour.echolist.data.models.UpdateFolderParams
import net.onefivefour.echolist.data.source.network.FakeFileRemoteDataSource
import net.onefivefour.echolist.network.error.NetworkException

class FileRepositoryImplTest : FunSpec({

    // -- Generators --

    val arbProtoFolder = arbitrary {
        `file`.v1.Folder(
            path = Arb.string(1..100).bind(),
            name = Arb.string(1..100).bind()
        )
    }

    val arbCreateFolderParams = arbitrary {
        CreateFolderParams(
            parentPath = Arb.string(0..100).bind(),
            name = Arb.string(1..100).bind()
        )
    }

    val arbUpdateFolderParams = arbitrary {
        UpdateFolderParams(
            folderPath = Arb.string(1..100).bind(),
            newName = Arb.string(1..100).bind()
        )
    }

    val arbDeleteFolderParams = arbitrary {
        DeleteFolderParams(
            folderPath = Arb.string(1..100).bind()
        )
    }


    // -- CreateFolder --

    test("createFolder returns mapped folder on success").config(invocations = 20) {
        checkAll(arbCreateFolderParams, arbProtoFolder) { params, protoFolder ->
            val fake = FakeFileRemoteDataSource()
            fake.createFolderResult = Result.success(CreateFolderResponse(folder = protoFolder))
            val repo = FileRepositoryImpl(fake, Dispatchers.Unconfined)

            val result = repo.createFolder(params)

            result.isSuccess shouldBe true
            val folder = result.getOrThrow()
            folder.path shouldBe protoFolder.path
            folder.name shouldBe protoFolder.name
        }
    }

    test("createFolder forwards correct proto fields to data source").config(invocations = 20) {
        checkAll(arbCreateFolderParams) { params ->
            val fake = FakeFileRemoteDataSource()
            fake.createFolderResult = Result.success(CreateFolderResponse(folder = `file`.v1.Folder(path = "/test/", name = "test")))
            val repo = FileRepositoryImpl(fake, Dispatchers.Unconfined)

            repo.createFolder(params)

            fake.lastCreateRequest?.parent_path shouldBe params.parentPath
            fake.lastCreateRequest?.name shouldBe params.name
        }
    }

    test("createFolder returns failure when network throws") {
        val fake = FakeFileRemoteDataSource()
        fake.createFolderResult = Result.failure(NetworkException.ServerError(500, "boom"))
        val repo = FileRepositoryImpl(fake, Dispatchers.Unconfined)

        val result = repo.createFolder(CreateFolderParams("", "n"))

        result.isFailure shouldBe true
        result.exceptionOrNull().shouldBeInstanceOf<NetworkException.ServerError>()
    }

    // -- UpdateFolder --

    test("updateFolder returns mapped folder on success").config(invocations = 20) {
        checkAll(arbUpdateFolderParams, arbProtoFolder) { params, protoFolder ->
            val fake = FakeFileRemoteDataSource()
            fake.updateFolderResult = Result.success(UpdateFolderResponse(folder = protoFolder))
            val repo = FileRepositoryImpl(fake, Dispatchers.Unconfined)

            val result = repo.updateFolder(params)

            result.isSuccess shouldBe true
            val folder = result.getOrThrow()
            folder.path shouldBe protoFolder.path
            folder.name shouldBe protoFolder.name
        }
    }

    test("updateFolder forwards correct proto fields to data source").config(invocations = 20) {
        checkAll(arbUpdateFolderParams) { params ->
            val fake = FakeFileRemoteDataSource()
            fake.updateFolderResult = Result.success(UpdateFolderResponse(folder = `file`.v1.Folder(path = "/new/", name = "new")))
            val repo = FileRepositoryImpl(fake, Dispatchers.Unconfined)

            repo.updateFolder(params)

            fake.lastUpdateRequest?.folder_path shouldBe params.folderPath
            fake.lastUpdateRequest?.new_name shouldBe params.newName
        }
    }

    test("updateFolder returns failure when network throws") {
        val fake = FakeFileRemoteDataSource()
        fake.updateFolderResult = Result.failure(NetworkException.ClientError(404, "not found"))
        val repo = FileRepositoryImpl(fake, Dispatchers.Unconfined)

        val result = repo.updateFolder(UpdateFolderParams("p/", "n"))

        result.isFailure shouldBe true
        result.exceptionOrNull().shouldBeInstanceOf<NetworkException.ClientError>()
    }

    // -- DeleteFolder --

    test("deleteFolder returns Unit on success").config(invocations = 20) {
        checkAll(arbDeleteFolderParams) { params ->
            val fake = FakeFileRemoteDataSource()
            fake.deleteFolderResult = Result.success(DeleteFolderResponse())
            val repo = FileRepositoryImpl(fake, Dispatchers.Unconfined)

            val result = repo.deleteFolder(params)

            result.isSuccess shouldBe true
            result.getOrThrow() shouldBe Unit
        }
    }

    test("deleteFolder forwards correct proto fields to data source").config(invocations = 20) {
        checkAll(arbDeleteFolderParams) { params ->
            val fake = FakeFileRemoteDataSource()
            val repo = FileRepositoryImpl(fake, Dispatchers.Unconfined)

            repo.deleteFolder(params)

            fake.lastDeleteRequest?.folder_path shouldBe params.folderPath
        }
    }

    test("deleteFolder returns failure when network throws") {
        val fake = FakeFileRemoteDataSource()
        fake.deleteFolderResult = Result.failure(NetworkException.NetworkError("timeout"))
        val repo = FileRepositoryImpl(fake, Dispatchers.Unconfined)

        val result = repo.deleteFolder(DeleteFolderParams("p/"))

        result.isFailure shouldBe true
        result.exceptionOrNull().shouldBeInstanceOf<NetworkException.NetworkError>()
    }
})
