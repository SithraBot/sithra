package top.ninnana.utils

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import top.ninnana.bean.Event
import top.ninnana.bean.GhostEvent
import top.ninnana.bean.LifecycleEvent
import top.ninnana.bean.MessageEvent

val json = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
    encodeDefaults = true
}

inline fun <reified T> T.json(): String {
    return json.encodeToString(this)
}

fun String.decodeToEvent(): Event? {
    try {
        val ghost = json.decodeFromString<GhostEvent>(this)
        return when (ghost.post_type) {
            "message" ->
                json.decodeFromString<MessageEvent>(this)

            "meta_event" -> {
                when (ghost.meta_event_type) {
                    "lifecycle" ->
                        json.decodeFromString<LifecycleEvent>(this)

                    else -> null
                }
            }

            else -> null
        }
    } catch (_: Exception) {
        //TODO: ECHO DATA
        return null
    }
}