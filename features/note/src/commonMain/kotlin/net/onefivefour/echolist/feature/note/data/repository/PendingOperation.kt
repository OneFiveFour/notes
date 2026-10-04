package net.onefivefour.echolist.feature.note.data.repository

import net.onefivefour.echolist.feature.note.domain.model.CreateNoteParams
import net.onefivefour.echolist.feature.note.domain.model.UpdateNoteParams

/**
 * Represents a write operation queued while offline.
 */
internal sealed class PendingOperation {
    data class Create(val params: CreateNoteParams) : PendingOperation()
    data class Update(val params: UpdateNoteParams) : PendingOperation()
}