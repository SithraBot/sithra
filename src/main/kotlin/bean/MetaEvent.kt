package top.ninnana.bean

import kotlinx.serialization.*

@Serializable
sealed class MetaEvent : Event() {
    override val post_type: String = "meta_event"
    abstract val meta_event_type: String
}

@Serializable
data class LifecycleEvent(
    override val time: Long,
    override val self_id: Long,
    val sub_type: String
) : MetaEvent() {
    override val meta_event_type: String = "lifecycle"
}