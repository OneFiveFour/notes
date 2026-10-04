package net.onefivefour.echolist.core.files.di

import net.onefivefour.echolist.core.files.domain.DirectoryChangeNotifier
import net.onefivefour.echolist.core.files.data.DirectoryChangeNotifierImpl
import org.koin.dsl.module

val filesModule = module {
    single<DirectoryChangeNotifier> { DirectoryChangeNotifierImpl() }
}