package net.onefivefour.echolist

import android.app.Application
import net.onefivefour.echolist.di.initKoin
import org.koin.android.ext.koin.androidContext

class EchoListApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin(
            platformConfiguration = {
                androidContext(this@EchoListApplication)
            }
        )
    }
}
