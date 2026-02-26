package net.onefivefour.echolist.ui.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

@Serializable
data object LoginRoute : NavKey

@Serializable
data class HomeRoute(val path: String = "/") : NavKey

@Serializable
data class EditNoteRoute(val noteId: String? = null) : NavKey

@Serializable
data class EditTaskListRoute(val taskListId: String? = null) : NavKey

val navKeySerializersModule = SerializersModule {
    polymorphic(NavKey::class) {
        subclass(LoginRoute::class, LoginRoute.serializer())
        subclass(HomeRoute::class, HomeRoute.serializer())
        subclass(EditNoteRoute::class, EditNoteRoute.serializer())
        subclass(EditTaskListRoute::class, EditTaskListRoute.serializer())
    }
}

val echoListSavedStateConfig = SavedStateConfiguration {
    serializersModule = navKeySerializersModule
}
