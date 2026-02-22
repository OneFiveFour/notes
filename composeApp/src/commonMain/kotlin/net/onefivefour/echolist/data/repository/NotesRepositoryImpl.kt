package net.onefivefour.echolist.data.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import net.onefivefour.echolist.data.mapper.NoteMapper
import net.onefivefour.echolist.data.models.CreateNoteParams
import net.onefivefour.echolist.data.models.Note
import net.onefivefour.echolist.data.models.UpdateNoteParams
import net.onefivefour.echolist.data.source.cache.CacheDataSource
import net.onefivefour.echolist.data.source.network.NetworkDataSource
import net.onefivefour.echolist.network.error.NetworkException
import notes.v1.DeleteNoteRequest
import notes.v1.GetNoteRequest
import notes.v1.ListNotesRequest

internal class NotesRepositoryImpl(
    private val networkDataSource: NetworkDataSource,
    private val cacheDataSource: CacheDataSource,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val backgroundScope: CoroutineScope = CoroutineScope(SupervisorJob() + dispatcher)
) : NotesRepository {

    private val mutex = Mutex()
    private val pendingOperations = mutableListOf<PendingOperation>()

    override suspend fun createNote(params: CreateNoteParams): Result<Note> = withContext(dispatcher) {
        try {
            val request = NoteMapper.toProto(params)
            val response = networkDataSource.createNote(request)
            val note = NoteMapper.toDomain(response)
            cacheDataSource.saveNote(note)
            Result.success(note)
        } catch (e: NetworkException) {
            // Queue for offline sync
            mutex.withLock {
                pendingOperations.add(PendingOperation.Create(params))
            }
            Result.failure(e)
        }
    }

    override suspend fun listNotes(path: String): Result<List<Note>> = withContext(dispatcher) {
        val cached = cacheDataSource.listNotes(path)
        if (cached.isNotEmpty()) {
            // Return cached data immediately, refresh in background
            backgroundScope.launch {
                try {
                    val request = ListNotesRequest(path = path)
                    val response = networkDataSource.listNotes(request)
                    val notes = response.notes.map { NoteMapper.toDomain(it) }
                    cacheDataSource.saveNotes(notes)
                } catch (_: Exception) {
                    // Background refresh failure is non-fatal
                }
            }
            Result.success(cached)
        } else {
            // No cache — go to network directly
            try {
                val request = ListNotesRequest(path = path)
                val response = networkDataSource.listNotes(request)
                val notes = response.notes.map { NoteMapper.toDomain(it) }
                cacheDataSource.saveNotes(notes)
                Result.success(notes)
            } catch (e: NetworkException) {
                Result.failure(e)
            }
        }
    }

    override suspend fun getNote(filePath: String): Result<Note> = withContext(dispatcher) {
        val cached = cacheDataSource.getNote(filePath)
        if (cached != null) {
            // Return cached data immediately, refresh in background
            backgroundScope.launch {
                try {
                    val request = GetNoteRequest(file_path = filePath)
                    val response = networkDataSource.getNote(request)
                    val note = NoteMapper.toDomain(response)
                    cacheDataSource.saveNote(note)
                } catch (_: Exception) {
                    // Background refresh failure is non-fatal
                }
            }
            Result.success(cached)
        } else {
            // No cache — go to network directly
            try {
                val request = GetNoteRequest(file_path = filePath)
                val response = networkDataSource.getNote(request)
                val note = NoteMapper.toDomain(response)
                cacheDataSource.saveNote(note)
                Result.success(note)
            } catch (e: NetworkException) {
                Result.failure(e)
            }
        }
    }

    override suspend fun updateNote(params: UpdateNoteParams): Result<Note> = withContext(dispatcher) {
        try {
            val request = NoteMapper.toProto(params)
            val response = networkDataSource.updateNote(request)
            // Fetch the full updated note from network
            val getResponse = networkDataSource.getNote(GetNoteRequest(file_path = params.filePath))
            val note = NoteMapper.toDomain(getResponse)
            cacheDataSource.saveNote(note)
            Result.success(note)
        } catch (e: NetworkException) {
            // Queue for offline sync
            mutex.withLock {
                pendingOperations.add(PendingOperation.Update(params))
            }
            Result.failure(e)
        }
    }

    override suspend fun deleteNote(filePath: String): Result<Unit> = withContext(dispatcher) {
        try {
            val request = DeleteNoteRequest(file_path = filePath)
            networkDataSource.deleteNote(request)
            cacheDataSource.deleteNote(filePath)
            Result.success(Unit)
        } catch (e: NetworkException) {
            Result.failure(e)
        }
    }

    /**
     * Returns a snapshot of pending offline operations.
     */
    internal suspend fun getPendingOperations(): List<PendingOperation> {
        return mutex.withLock {
            pendingOperations.toList()
        }
    }

    /**
     * Syncs all pending offline operations in FIFO order.
     * Successfully synced operations are removed from the queue.
     * Stops on the first failure and throws the exception.
     */
    internal suspend fun syncPendingOperations() = withContext(dispatcher) {
        val ops = mutex.withLock { pendingOperations.toList() }
        for (op in ops) {
            when (op) {
                is PendingOperation.Create -> {
                    val request = NoteMapper.toProto(op.params)
                    val response = networkDataSource.createNote(request)
                    val note = NoteMapper.toDomain(response)
                    cacheDataSource.saveNote(note)
                }
                is PendingOperation.Update -> {
                    val request = NoteMapper.toProto(op.params)
                    networkDataSource.updateNote(request)
                    val getResponse = networkDataSource.getNote(
                        GetNoteRequest(file_path = op.params.filePath)
                    )
                    val note = NoteMapper.toDomain(getResponse)
                    cacheDataSource.saveNote(note)
                }
            }
            mutex.withLock {
                pendingOperations.remove(op)
            }
        }
    }
}
