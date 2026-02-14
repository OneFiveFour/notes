package net.onefivefour.notes

import android.app.Application
import net.onefivefour.notes.di.initKoin
import org.koin.android.ext.koin.androidContext

class NotesApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin(
            platformConfiguration = {
                androidContext(this@NotesApplication)
            }
        )
    }
}
