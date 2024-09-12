package top.ninnana.bean

import kotlinx.serialization.*

@Serializable
data class GhostEvent(
    override val self_id: Long,
    override val time: Long,
    override val post_type: String,
    val meta_event_type: String?
) : Event()