package net.onefivefour.echolist.data.mapper

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import net.onefivefour.echolist.data.models.CreateFolderParams
import net.onefivefour.echolist.data.models.DeleteFolderParams
import net.onefivefour.echolist.data.models.RenameFolderParams

class FolderMapperPropertyTest : FunSpec({

    // -- Generators --

    val arbPath = Arb.string(1..100)

    val arbFolderEntry = arbitrary {
        folder.v1.FolderEntry(path = arbPath.bind())
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

    // -- Proto -> Domain --

    test("FolderEntry proto -> domain preserves path").config(invocations = 20) {
        checkAll(arbFolderEntry) { proto ->
            val domain = FolderMapper.toDomain(proto)
            domain.path shouldBe proto.path
        }
    }

    test("CreateFolderResponse -> domain preserves all entries").config(invocations = 20) {
        checkAll(Arb.list(arbFolderEntry, 0..10)) { entries ->
            val response = folder.v1.CreateFolderResponse(entries = entries)
            val domain = FolderMapper.toDomain(response)
            domain shouldHaveSize entries.size
            domain.zip(entries).forEach { (d, p) ->
                d.path shouldBe p.path
            }
        }
    }

    test("RenameFolderResponse -> domain preserves all entries").config(invocations = 20) {
        checkAll(Arb.list(arbFolderEntry, 0..10)) { entries ->
            val response = folder.v1.RenameFolderResponse(entries = entries)
            val domain = FolderMapper.toDomain(response)
            domain shouldHaveSize entries.size
            domain.zip(entries).forEach { (d, p) ->
                d.path shouldBe p.path
            }
        }
    }

    test("DeleteFolderResponse -> domain preserves all entries").config(invocations = 20) {
        checkAll(Arb.list(arbFolderEntry, 0..10)) { entries ->
            val response = folder.v1.DeleteFolderResponse(entries = entries)
            val domain = FolderMapper.toDomain(response)
            domain shouldHaveSize entries.size
            domain.zip(entries).forEach { (d, p) ->
                d.path shouldBe p.path
            }
        }
    }

    // -- Domain -> Proto --

    test("CreateFolderParams -> CreateFolderRequest preserves all fields").config(invocations = 20) {
        checkAll(arbCreateFolderParams) { params ->
            val proto = FolderMapper.toProto(params)
            proto.domain shouldBe params.domain
            proto.parent_path shouldBe params.parentPath
            proto.name shouldBe params.name
        }
    }

    test("RenameFolderParams -> RenameFolderRequest preserves all fields").config(invocations = 20) {
        checkAll(arbRenameFolderParams) { params ->
            val proto = FolderMapper.toProto(params)
            proto.domain shouldBe params.domain
            proto.folder_path shouldBe params.folderPath
            proto.new_name shouldBe params.newName
        }
    }

    test("DeleteFolderParams -> DeleteFolderRequest preserves all fields").config(invocations = 20) {
        checkAll(arbDeleteFolderParams) { params ->
            val proto = FolderMapper.toProto(params)
            proto.domain shouldBe params.domain
            proto.folder_path shouldBe params.folderPath
        }
    }
})
