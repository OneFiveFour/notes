package net.onefivefour.echolist.data.repository

import `file`.v1.CreateFolderResponse
import `file`.v1.DeleteFolderResponse
import `file`.v1.ListFilesResponse
import `file`.v1.UpdateFolderResponse
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.Dispatchers
import net.onefivefour.echolist.data.models.CreateFolderParams
import net.onefivefour.echolist.data.models.DeleteFolderParams
import net.onefivefour.echolist.data.models.UpdateFolderParams
import net.onefivefour.echolist.data.source.network.FakeFileRemoteDataSource
import net.onefivefour.echolist.network.error.NetworkException

/**
 * Feature: proto-api-update, Property 4: FileRepositoryImpl success delegation
 * Feature: proto-api-update, Property 5: FileRepositoryImpl error propagation
 *
 * **Validates: Requirements 4.3, 4.4, 4.5, 4.6, 4.7**
 */
class FileRepositoryImplPropertyTest : FunSpec({

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

    val arbEntries = Arb.list(Arb.string(0..100), 0..20)

    val arbException: Arb<Exception> = arbitrary {
        val idx = Arb.int(0..4).bind()
        val msg = Arb.string(1..50).bind()
        when (idx) {
            0 -> NetworkException.NetworkError(msg)
            1 -> NetworkException.ServerError(500, msg)
            2 -> NetworkException.ClientError(400, msg)
            3 -> NetworkException.TimeoutError(msg)
            else -> NetworkException.SerializationError(msg)
        }
    }

    // ---------------------------------------------------------------
    // Property 4: FileRepositoryImpl success delegation
    // ---------------------------------------------------------------

    // Feature: proto-api-update, Property 4: FileRepositoryImpl success delegation
    // **Validates: Requirements 4.3, 4.4, 4.5, 4.6**

    test("Property 4: createFolder returns Result.success with correctly mapped Folder") {
        checkAll(
            PropTestConfig(iterations = 100),
            arbCreateFolderParams,
            arbProtoFolder
        ) { params, protoFolder ->
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

    test("Property 4: listFiles returns Result.success with correctly mapped entries") {
        checkAll(
            PropTestConfig(iterations = 100),
            Arb.string(0..100),
            arbEntries
        ) { parentPath, entries ->
            val fake = FakeFileRemoteDataSource()
            fake.listFilesResult = Result.success(ListFilesResponse(entries = entries))
            val repo = FileRepositoryImpl(fake, Dispatchers.Unconfined)

            val result = repo.listFiles(parentPath)

            result.isSuccess shouldBe true
            result.getOrThrow() shouldBe entries
        }
    }

    test("Property 4: updateFolder returns Result.success with correctly mapped Folder") {
        checkAll(
            PropTestConfig(iterations = 100),
            arbUpdateFolderParams,
            arbProtoFolder
        ) { params, protoFolder ->
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

    test("Property 4: deleteFolder returns Result.success(Unit) on success") {
        checkAll(
            PropTestConfig(iterations = 100),
            arbDeleteFolderParams
        ) { params ->
            val fake = FakeFileRemoteDataSource()
            fake.deleteFolderResult = Result.success(DeleteFolderResponse())
            val repo = FileRepositoryImpl(fake, Dispatchers.Unconfined)

            val result = repo.deleteFolder(params)

            result.isSuccess shouldBe true
            result.getOrThrow() shouldBe Unit
        }
    }

    // ---------------------------------------------------------------
    // Property 5: FileRepositoryImpl error propagation
    // ---------------------------------------------------------------

    // Feature: proto-api-update, Property 5: FileRepositoryImpl error propagation
    // **Validates: Requirements 4.7**

    test("Property 5: createFolder returns Result.failure wrapping the data source exception") {
        checkAll(
            PropTestConfig(iterations = 100),
            arbCreateFolderParams,
            arbException
        ) { params, exception ->
            val fake = FakeFileRemoteDataSource()
            fake.createFolderResult = Result.failure(exception)
            val repo = FileRepositoryImpl(fake, Dispatchers.Unconfined)

            val result = repo.createFolder(params)

            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe exception
        }
    }

    test("Property 5: listFiles returns Result.failure wrapping the data source exception") {
        checkAll(
            PropTestConfig(iterations = 100),
            Arb.string(0..100),
            arbException
        ) { parentPath, exception ->
            val fake = FakeFileRemoteDataSource()
            fake.listFilesResult = Result.failure(exception)
            val repo = FileRepositoryImpl(fake, Dispatchers.Unconfined)

            val result = repo.listFiles(parentPath)

            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe exception
        }
    }

    test("Property 5: updateFolder returns Result.failure wrapping the data source exception") {
        checkAll(
            PropTestConfig(iterations = 100),
            arbUpdateFolderParams,
            arbException
        ) { params, exception ->
            val fake = FakeFileRemoteDataSource()
            fake.updateFolderResult = Result.failure(exception)
            val repo = FileRepositoryImpl(fake, Dispatchers.Unconfined)

            val result = repo.updateFolder(params)

            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe exception
        }
    }

    test("Property 5: deleteFolder returns Result.failure wrapping the data source exception") {
        checkAll(
            PropTestConfig(iterations = 100),
            arbDeleteFolderParams,
            arbException
        ) { params, exception ->
            val fake = FakeFileRemoteDataSource()
            fake.deleteFolderResult = Result.failure(exception)
            val repo = FileRepositoryImpl(fake, Dispatchers.Unconfined)

            val result = repo.deleteFolder(params)

            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe exception
        }
    }
})
