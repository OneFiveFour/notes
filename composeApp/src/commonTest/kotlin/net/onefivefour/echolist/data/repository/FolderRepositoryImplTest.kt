package net.onefivefour.echolist.data.repository

import folder.v1.CreateFolderResponse
import folder.v1.DeleteFolderResponse
import folder.v1.RenameFolderResponse
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.Dispatchers
import net.onefivefour.echolist.data.models.CreateFolderParams
import net.onefivefour.echolist.data.models.DeleteFolderParams
import net.onefivefour.echolist.data.models.RenameFolderParams
import net.onefivefour.echolist.data.source.network.FakeFolderNetworkDataSource
import net.onefivefour.echolist.network.error.NetworkException

class FolderRepositoryImplTest : FunSpec({

    // -- Generators --

    val arbDirectoryEntry = arbitrary {
        folder.v1.DirectoryEntry(path = Arb.string(1..100).bind())
    }

    val arbCreateFolderParams = arbitrary {
        CreateFolderParams(
            domain = Arb.string(1..50).bind(),
            parentPath = Arb.string(0..100).bind(),
            name = Arb.string(1..100).bind()
        )
    }

    val arbRenameFolderParams = arbitrary {
        RenameFolderParams(
            domain = Arb.string(1..50).bind(),
            folderPath = Arb.string(1..100).bind(),
            newName = Arb.string(1..100).bind()
        )
    }

    val arbDeleteFolderParams = arbitrary {
        DeleteFolderParams(
            domain = Arb.string(1..50).bind(),
            folderPath = Arb.string(1..100).bind()
        )
    }

    // -- CreateFolder --

    test("createFolder returns mapped directory entries on success").config(invocations = 20) {
        checkAll(arbCreateFolderParams, Arb.list(arbDirectoryEntry, 0..5)) { params, entries ->
            val fake = FakeFolderNetworkDataSource()
            fake.createFolderResult = Result.success(CreateFolderResponse(entries = entries))
            val repo = FolderRepositoryImpl(fake, Dispatchers.Unconfined)

            val result = repo.createFolder(params)

            result.isSuccess shouldBe true
            val domainEntries = result.getOrThrow()
            domainEntries shouldHaveSize entries.size
            domainEntries.zip(entries).forEach { (d, p) ->
                d.path shouldBe p.path
            }
        }
    }

    test("createFolder forwards correct proto fields to data source").config(invocations = 20) {
        checkAll(arbCreateFolderParams) { params ->
            val fake = FakeFolderNetworkDataSource()
            val repo = FolderRepositoryImpl(fake, Dispatchers.Unconfined)

            repo.createFolder(params)

            fake.lastCreateRequest?.domain shouldBe params.domain
            fake.lastCreateRequest?.parent_path shouldBe params.parentPath
            fake.lastCreateRequest?.name shouldBe params.name
        }
    }

    test("createFolder returns failure when network throws") {
        val fake = FakeFolderNetworkDataSource()
        fake.createFolderResult = Result.failure(NetworkException.ServerError(500, "boom"))
        val repo = FolderRepositoryImpl(fake, Dispatchers.Unconfined)

        val result = repo.createFolder(CreateFolderParams("d", "", "n"))

        result.isFailure shouldBe true
        result.exceptionOrNull().shouldBeInstanceOf<NetworkException.ServerError>()
    }

    // -- RenameFolder --

    test("renameFolder returns mapped directory entries on success").config(invocations = 20) {
        checkAll(arbRenameFolderParams, Arb.list(arbDirectoryEntry, 0..5)) { params, entries ->
            val fake = FakeFolderNetworkDataSource()
            fake.renameFolderResult = Result.success(RenameFolderResponse(entries = entries))
            val repo = FolderRepositoryImpl(fake, Dispatchers.Unconfined)

            val result = repo.renameFolder(params)

            result.isSuccess shouldBe true
            val domainEntries = result.getOrThrow()
            domainEntries shouldHaveSize entries.size
            domainEntries.zip(entries).forEach { (d, p) ->
                d.path shouldBe p.path
            }
        }
    }

    test("renameFolder forwards correct proto fields to data source").config(invocations = 20) {
        checkAll(arbRenameFolderParams) { params ->
            val fake = FakeFolderNetworkDataSource()
            val repo = FolderRepositoryImpl(fake, Dispatchers.Unconfined)

            repo.renameFolder(params)

            fake.lastRenameRequest?.domain shouldBe params.domain
            fake.lastRenameRequest?.folder_path shouldBe params.folderPath
            fake.lastRenameRequest?.new_name shouldBe params.newName
        }
    }

    test("renameFolder returns failure when network throws") {
        val fake = FakeFolderNetworkDataSource()
        fake.renameFolderResult = Result.failure(NetworkException.ClientError(404, "not found"))
        val repo = FolderRepositoryImpl(fake, Dispatchers.Unconfined)

        val result = repo.renameFolder(RenameFolderParams("d", "p/", "n"))

        result.isFailure shouldBe true
        result.exceptionOrNull().shouldBeInstanceOf<NetworkException.ClientError>()
    }

    // -- DeleteFolder --

    test("deleteFolder returns mapped directory entries on success").config(invocations = 20) {
        checkAll(arbDeleteFolderParams, Arb.list(arbDirectoryEntry, 0..5)) { params, entries ->
            val fake = FakeFolderNetworkDataSource()
            fake.deleteFolderResult = Result.success(DeleteFolderResponse(entries = entries))
            val repo = FolderRepositoryImpl(fake, Dispatchers.Unconfined)

            val result = repo.deleteFolder(params)

            result.isSuccess shouldBe true
            val domainEntries = result.getOrThrow()
            domainEntries shouldHaveSize entries.size
            domainEntries.zip(entries).forEach { (d, p) ->
                d.path shouldBe p.path
            }
        }
    }

    test("deleteFolder forwards correct proto fields to data source").config(invocations = 20) {
        checkAll(arbDeleteFolderParams) { params ->
            val fake = FakeFolderNetworkDataSource()
            val repo = FolderRepositoryImpl(fake, Dispatchers.Unconfined)

            repo.deleteFolder(params)

            fake.lastDeleteRequest?.domain shouldBe params.domain
            fake.lastDeleteRequest?.folder_path shouldBe params.folderPath
        }
    }

    test("deleteFolder returns failure when network throws") {
        val fake = FakeFolderNetworkDataSource()
        fake.deleteFolderResult = Result.failure(NetworkException.NetworkError("timeout"))
        val repo = FolderRepositoryImpl(fake, Dispatchers.Unconfined)

        val result = repo.deleteFolder(DeleteFolderParams("d", "p/"))

        result.isFailure shouldBe true
        result.exceptionOrNull().shouldBeInstanceOf<NetworkException.NetworkError>()
    }
})
