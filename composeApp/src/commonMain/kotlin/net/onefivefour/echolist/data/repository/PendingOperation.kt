package net.onefivefour.echolist.data.repository

import net.onefivefour.echolist.data.models.CreateNoteParams
import net.onefivefour.echolist.data.models.UpdateNoteParams

/**
 * Represents a write operation queued while offline.
 */
internal sealed class PendingOperation {
    data class Create(val params: CreateNoteParams) : PendingOperation()
    data class Update(val params: UpdateNoteParams) : PendingOperation()
}
