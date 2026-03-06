package net.onefivefour.echolist.data.mapper

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import net.onefivefour.echolist.data.models.CreateFolderParams
import net.onefivefour.echolist.data.models.DeleteFolderParams
import net.onefivefour.echolist.data.models.UpdateFolderParams

/**
 * Unit tests for FileMapper transformations.
 * Tests specific examples, edge cases, and field name conversions.
 */
class FileMapperTest : FunSpec({

    // -- Proto -> Domain transformations --

    test("toDomain transforms proto Folder to domain Folder") {
        val protoFolder = `file`.v1.Folder(
            path = "/home/user/documents",
            name = "documents"
        )

        val domain = FileMapper.toDomain(protoFolder)

        domain.path shouldBe "/home/user/documents"
        domain.name shouldBe "documents"
    }

    test("toDomain transforms CreateFolderResponse to domain Folder") {
        val protoFolder = `file`.v1.Folder(
            path = "/home/user/projects",
            name = "projects"
        )
        val response = `file`.v1.CreateFolderResponse(folder = protoFolder)

        val domain = FileMapper.toDomain(response)

        domain.path shouldBe "/home/user/projects"
        domain.name shouldBe "projects"
    }

    test("toDomain transforms ListFilesResponse with multiple entries") {
        val response = `file`.v1.ListFilesResponse(
            entries = listOf("file1.txt", "file2.md", "folder1")
        )

        val domainList = FileMapper.toDomain(response)

        domainList shouldHaveSize 3
        domainList shouldBe listOf("file1.txt", "file2.md", "folder1")
    }

    test("toDomain transforms ListFilesResponse with empty entries") {
        val response = `file`.v1.ListFilesResponse(entries = emptyList())

        val domainList = FileMapper.toDomain(response)

        domainList.shouldBeEmpty()
    }

    test("toDomain transforms UpdateFolderResponse to domain Folder") {
        val protoFolder = `file`.v1.Folder(
            path = "/home/user/renamed",
            name = "renamed"
        )
        val response = `file`.v1.UpdateFolderResponse(folder = protoFolder)

        val domain = FileMapper.toDomain(response)

        domain.path shouldBe "/home/user/renamed"
        domain.name shouldBe "renamed"
    }

    // -- Domain -> Proto transformations --

    test("toProto transforms CreateFolderParams to CreateFolderRequest with snake_case fields") {
        val params = CreateFolderParams(
            parentDir = "/home/user",
            name = "new_folder"
        )

        val proto = FileMapper.toProto(params)

        proto.parent_dir shouldBe "/home/user"
        proto.name shouldBe "new_folder"
    }

    test("toProto transforms UpdateFolderParams to UpdateFolderRequest with snake_case fields") {
        val params = UpdateFolderParams(
            folderPath = "/home/user/old_name",
            newName = "new_name"
        )

        val proto = FileMapper.toProto(params)

        proto.folder_path shouldBe "/home/user/old_name"
        proto.new_name shouldBe "new_name"
    }

    test("toProto transforms DeleteFolderParams to DeleteFolderRequest with snake_case fields") {
        val params = DeleteFolderParams(
            folderPath = "/home/user/to_delete"
        )

        val proto = FileMapper.toProto(params)

        proto.folder_path shouldBe "/home/user/to_delete"
    }

    // -- Edge cases --

    test("toDomain handles folder with empty name") {
        val protoFolder = `file`.v1.Folder(
            path = "/home/user",
            name = ""
        )

        val domain = FileMapper.toDomain(protoFolder)

        domain.path shouldBe "/home/user"
        domain.name shouldBe ""
    }

    test("toDomain handles folder with special characters in name") {
        val protoFolder = `file`.v1.Folder(
            path = "/home/user/special-folder_123",
            name = "special-folder_123"
        )

        val domain = FileMapper.toDomain(protoFolder)

        domain.path shouldBe "/home/user/special-folder_123"
        domain.name shouldBe "special-folder_123"
    }

    test("toDomain handles ListFilesResponse with single entry") {
        val response = `file`.v1.ListFilesResponse(entries = listOf("single.txt"))

        val domainList = FileMapper.toDomain(response)

        domainList shouldHaveSize 1
        domainList[0] shouldBe "single.txt"
    }
})