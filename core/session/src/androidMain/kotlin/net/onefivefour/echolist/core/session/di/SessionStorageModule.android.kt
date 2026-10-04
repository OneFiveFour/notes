package net.onefivefour.echolist.core.session.di
import net.onefivefour.echolist.core.session.data.storage.AndroidSecureStorage
import net.onefivefour.echolist.core.session.domain.SecureStorage
import org.koin.dsl.module
actual val sessionStorageModule = module {
    single<SecureStorage> { AndroidSecureStorage(context = get()) }
}