package net.onefivefour.notes.data.repository

import net.onefivefour.notes.data.models.CreateNoteParams
import net.onefivefour.notes.data.models.UpdateNoteParams

/**
 * Represents a write operation queued while offline.
 */
internal sealed class PendingOperation {
    data class Create(val params: CreateNoteParams) : PendingOperation()
    data class Update(val params: UpdateNoteParams) : PendingOperation()
}
