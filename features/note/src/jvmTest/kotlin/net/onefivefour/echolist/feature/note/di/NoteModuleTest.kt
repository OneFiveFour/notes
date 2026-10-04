package net.onefivefour.echolist.feature.note.di

import io.kotest.core.spec.style.FunSpec
import net.onefivefour.echolist.core.files.domain.DirectoryChangeNotifier
import net.onefivefour.echolist.core.networking.data.client.ConnectRpcClient
import net.onefivefour.echolist.feature.note.ui.EditNoteMode
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.test.verify.verify

@OptIn(KoinExperimentalAPI::class)
class NoteModuleTest : FunSpec({
    test("note module resolves with its declared platform services and editor parameter") {
        noteModule.verify(
            extraTypes = listOf(
                ConnectRpcClient::class,
                net.onefivefour.echolist.core.database.data.DatabaseProvider::class,
                DirectoryChangeNotifier::class,
                EditNoteMode::class,
                kotlinx.coroutines.CoroutineDispatcher::class
            )
        )
    }
})