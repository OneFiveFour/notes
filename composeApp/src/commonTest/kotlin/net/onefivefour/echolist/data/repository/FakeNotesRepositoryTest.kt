package net.onefivefour.echolist.data.repository

import kotlinx.coroutines.test.runTest
import net.onefivefour.echolist.data.models.CreateNoteParams
import net.onefivefour.echolist.data.models.Note
import net.onefivefour.echolist.data.models.UpdateNoteParams
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FakeNotesRepositoryTest {

    private val sampleNote = Note("docs/hello.md", "hello", "world", 100L)

    @Test
    fun preConfiguredNoteIsReturnedByGetNote() = runTest {
        val repo = FakeNotesRepository()
        repo.addNotes(sampleNote)

        val result = repo.getNote(sampleNote.filePath)

        assertTrue(result.isSuccess)
        assertEquals(sampleNote, result.getOrNull())
    }

    @Test
    fun preConfiguredNotesAreReturnedByListNotes() = runTest {
        val repo = FakeNotesRepository()
        val note2 = Note("docs/second.md", "second", "content", 200L)
        repo.addNotes(sampleNote, note2)

        val result = repo.listNotes()

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
    }

    @Test
    fun createNoteReturnsCreatedNote() = runTest {
        val repo = FakeNotesRepository()
        val params = CreateNoteParams("title", "body", "/docs")

        val result = repo.createNote(params)

        assertTrue(result.isSuccess)
        assertEquals("title", result.getOrNull()?.title)
        assertEquals("body", result.getOrNull()?.content)
    }

    @Test
    fun setShouldFailCausesAllOperationsToFail() = runTest {
        val repo = FakeNotesRepository()
        val error = RuntimeException("simulated")
        repo.setShouldFail(error)

        assertTrue(repo.createNote(CreateNoteParams("t", "c", "/p")).isFailure)
        assertTrue(repo.listNotes().isFailure)
        assertTrue(repo.getNote("any").isFailure)
        assertTrue(repo.updateNote(UpdateNoteParams("any", "c")).isFailure)
        assertTrue(repo.deleteNote("any").isFailure)
    }

    @Test
    fun clearingShouldFailRestoresNormalBehavior() = runTest {
        val repo = FakeNotesRepository()
        repo.addNotes(sampleNote)
        repo.setShouldFail(RuntimeException("fail"))

        assertTrue(repo.getNote(sampleNote.filePath).isFailure)

        repo.setShouldFail(null)

        assertTrue(repo.getNote(sampleNote.filePath).isSuccess)
    }

    @Test
    fun callLogTracksAllInvocations() = runTest {
        val repo = FakeNotesRepository()
        repo.addNotes(sampleNote)

        repo.createNote(CreateNoteParams("t", "c", "/p"))
        repo.listNotes("docs")
        repo.getNote("docs/hello.md")
        repo.updateNote(UpdateNoteParams("docs/hello.md", "new"))
        repo.deleteNote("docs/hello.md")

        assertEquals(5, repo.callLog.size)
        assertTrue(repo.callLog[0].startsWith("createNote("))
        assertTrue(repo.callLog[1].startsWith("listNotes("))
        assertTrue(repo.callLog[2].startsWith("getNote("))
        assertTrue(repo.callLog[3].startsWith("updateNote("))
        assertTrue(repo.callLog[4].startsWith("deleteNote("))
    }

    @Test
    fun callLogRecordsEvenWhenFailing() = runTest {
        val repo = FakeNotesRepository()
        repo.setShouldFail(RuntimeException("err"))

        repo.getNote("x")
        repo.listNotes()

        assertEquals(2, repo.callLog.size)
    }
}
