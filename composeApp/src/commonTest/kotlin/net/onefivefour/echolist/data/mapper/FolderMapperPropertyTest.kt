package net.onefivefour.echolist.data.mapper

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import net.onefivefour.echolist.data.models.CreateFolderParams
import net.onefivefour.echolist.data.models.DeleteFolderParams
import net.onefivefour.echolist.data.models.UpdateFolderParams

/**
 * Feature: proto-api-update
 * Property 1: Folder mapper domain-to-proto field preservation
 * Property 2: Folder mapper proto-to-domain field preservation
 *
 * Validates: Requirements 1.4, 1.5, 1.6, 3.2, 3.3, 3.4, 3.5, 3.6
 */
class FolderMapperPropertyTest : FunSpec({

    // -- Generators --

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

    val arbProtoFolder = arbitrary {
        folder.v1.Folder(
            path = Arb.string(1..100).bind(),
            name = Arb.string(1..100).bind()
        )
    }

    // -- Property 1: Domain -> Proto field preservation --

    test("Feature: proto-api-update, Property 1: CreateFolderParams -> CreateFolderRequest preserves all fields") {
        checkAll(PropTestConfig(iterations = 100), arbCreateFolderParams) { params ->
            val proto = FolderMapper.toProto(params)
            proto.parent_path shouldBe params.parentPath
            proto.name shouldBe params.name
        }
    }

    test("Feature: proto-api-update, Property 1: UpdateFolderParams -> UpdateFolderRequest preserves all fields") {
        checkAll(PropTestConfig(iterations = 100), arbUpdateFolderParams) { params ->
            val proto = FolderMapper.toProto(params)
            proto.folder_path shouldBe params.folderPath
            proto.new_name shouldBe params.newName
        }
    }

    test("Feature: proto-api-update, Property 1: DeleteFolderParams -> DeleteFolderRequest preserves all fields") {
        checkAll(PropTestConfig(iterations = 100), arbDeleteFolderParams) { params ->
            val proto = FolderMapper.toProto(params)
            proto.folder_path shouldBe params.folderPath
        }
    }

    // -- Property 2: Proto -> Domain field preservation --

    test("Feature: proto-api-update, Property 2: proto Folder -> domain Folder preserves path and name") {
        checkAll(PropTestConfig(iterations = 100), arbProtoFolder) { proto ->
            val domain = FolderMapper.toDomain(proto)
            domain.path shouldBe proto.path
            domain.name shouldBe proto.name
        }
    }

    test("Feature: proto-api-update, Property 2: CreateFolderResponse -> domain Folder preserves path and name") {
        checkAll(PropTestConfig(iterations = 100), arbProtoFolder) { protoFolder ->
            val response = folder.v1.CreateFolderResponse(folder = protoFolder)
            val domain = FolderMapper.toDomain(response)
            domain.path shouldBe protoFolder.path
            domain.name shouldBe protoFolder.name
        }
    }

    test("Feature: proto-api-update, Property 2: GetFolderResponse -> domain Folder preserves path and name") {
        checkAll(PropTestConfig(iterations = 100), arbProtoFolder) { protoFolder ->
            val response = folder.v1.GetFolderResponse(folder = protoFolder)
            val domain = FolderMapper.toDomain(response)
            domain.path shouldBe protoFolder.path
            domain.name shouldBe protoFolder.name
        }
    }

    test("Feature: proto-api-update, Property 2: ListFoldersResponse -> domain list preserves all folders") {
        checkAll(PropTestConfig(iterations = 100), Arb.list(arbProtoFolder, 0..10)) { protoFolders ->
            val response = folder.v1.ListFoldersResponse(folders = protoFolders)
            val domainList = FolderMapper.toDomain(response)
            domainList shouldHaveSize protoFolders.size
            domainList.zip(protoFolders).forEach { (domain, proto) ->
                domain.path shouldBe proto.path
                domain.name shouldBe proto.name
            }
        }
    }

    test("Feature: proto-api-update, Property 2: UpdateFolderResponse -> domain Folder preserves path and name") {
        checkAll(PropTestConfig(iterations = 100), arbProtoFolder) { protoFolder ->
            val response = folder.v1.UpdateFolderResponse(folder = protoFolder)
            val domain = FolderMapper.toDomain(response)
            domain.path shouldBe protoFolder.path
            domain.name shouldBe protoFolder.name
        }
    }
})
