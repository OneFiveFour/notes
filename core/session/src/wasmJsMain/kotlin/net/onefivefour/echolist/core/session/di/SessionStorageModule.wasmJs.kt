package net.onefivefour.echolist.core.session.di
import net.onefivefour.echolist.core.session.domain.SecureStorage
import net.onefivefour.echolist.core.session.data.storage.WasmJsSecureStorage
import org.koin.dsl.module
actual val sessionStorageModule = module {
    single<SecureStorage> { WasmJsSecureStorage() }
}