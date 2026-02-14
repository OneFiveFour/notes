package net.onefivefour.notes.data.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import net.onefivefour.notes.data.mapper.NoteMapper
import net.onefivefour.notes.data.models.CreateNoteParams
import net.onefivefour.notes.data.models.Note
import net.onefivefour.notes.data.models.UpdateNoteParams
import net.onefivefour.notes.data.source.cache.CacheDataSource
import net.onefivefour.notes.data.source.network.NetworkDataSource
import net.onefivefour.notes.network.error.NetworkException
import notes.v1.DeleteNoteRequest
import notes.v1.GetNoteRequest
import notes.v1.ListNotesRequest

internal class NotesRepositoryImpl(
    private val networkDataSource: NetworkDataSource,
    private val cacheDataSource: CacheDataSource,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
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
        try {
            val request = ListNotesRequest(path = path)
            val response = networkDataSource.listNotes(request)
            val notes = response.notes.map { NoteMapper.toDomain(it) }
            cacheDataSource.saveNotes(notes)
            Result.success(notes)
        } catch (e: NetworkException) {
            val cached = cacheDataSource.listNotes(path)
            if (cached.isNotEmpty()) {
                Result.success(cached)
            } else {
                Result.failure(e)
            }
        }
    }

    override suspend fun getNote(filePath: String): Result<Note> = withContext(dispatcher) {
        try {
            val request = GetNoteRequest(file_path = filePath)
            val response = networkDataSource.getNote(request)
            val note = NoteMapper.toDomain(response)
            cacheDataSource.saveNote(note)
            Result.success(note)
        } catch (e: NetworkException) {
            val cached = cacheDataSource.getNote(filePath)
            if (cached != null) {
                Result.success(cached)
            } else {
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
