package top.ninnana.bean

import kotlinx.serialization.*

@Serializable
abstract class Event {
    abstract val time: Long
    abstract val self_id: Long
    abstract val post_type: String
}